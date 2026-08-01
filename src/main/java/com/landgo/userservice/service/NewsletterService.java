package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.NewsletterPublishRequest;
import com.landgo.userservice.dto.request.NewsletterSubscribeRequest;
import com.landgo.userservice.dto.response.NewsletterCampaignDetailResponse;
import com.landgo.userservice.dto.response.NewsletterCampaignResponse;
import com.landgo.userservice.dto.response.NewsletterSubscribeResponse;
import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.entity.NewsletterSubscriber;
import com.landgo.userservice.enums.CampaignStatus;
import com.landgo.userservice.exception.ApiException;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.repository.NewsletterCampaignRepository;
import com.landgo.userservice.repository.NewsletterSubscriberRepository;
import com.landgo.userservice.util.NewsletterHtmlBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_UNSUBSCRIBED = "UNSUBSCRIBED";

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterCampaignRepository campaignRepository;
    private final NewsletterCampaignWriter campaignWriter;
    private final EmailService emailService;

    // --- public subscription ---

    @Transactional
    public NewsletterSubscribeResponse subscribe(NewsletterSubscribeRequest request) {
        String email = normalizeEmail(request.getEmail());
        Optional<NewsletterSubscriber> existingOpt = subscriberRepository.findByEmail(email);

        NewsletterSubscriber subscriber;
        if (existingOpt.isPresent()) {
            subscriber = existingOpt.get();
            if (STATUS_ACTIVE.equals(subscriber.getStatus())) {
                // Re-subscribing must stay idempotent — the footer form has no duplicate check.
                return NewsletterSubscribeResponse.builder()
                        .success(true)
                        .subscriberId(subscriber.getId())
                        .message("Already subscribed")
                        .build();
            }
            subscriber.setStatus(STATUS_ACTIVE);
            subscriber.setConsent(request.isConsent());
            if (subscriber.getUnsubscribeToken() == null) {
                subscriber.setUnsubscribeToken(UUID.randomUUID());
            }
        } else {
            subscriber = NewsletterSubscriber.builder()
                    .email(email)
                    .consent(request.isConsent())
                    .status(STATUS_ACTIVE)
                    .unsubscribeToken(UUID.randomUUID())
                    .build();
        }

        subscriber = subscriberRepository.save(subscriber);
        log.info("Newsletter subscription registered for: {}", subscriber.getEmail());

        return NewsletterSubscribeResponse.builder()
                .success(true)
                .subscriberId(subscriber.getId())
                .message("Subscribed successfully")
                .build();
    }

    @Transactional
    public void unsubscribe(String email) {
        subscriberRepository.findByEmail(normalizeEmail(email)).ifPresent(this::markUnsubscribed);
    }

    /** Unsubscribe via the tokenised link carried in every outbound newsletter. */
    @Transactional
    public void unsubscribeByToken(String token) {
        UUID parsed;
        try {
            parsed = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("NewsletterSubscriber", "unsubscribeToken", token);
        }

        NewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(parsed)
                .orElseThrow(() -> new ResourceNotFoundException("NewsletterSubscriber", "unsubscribeToken", token));

        markUnsubscribed(subscriber);
    }

    // --- admin publish ---

    /**
     * Validates the send, persists the campaign, and hands the broadcast to
     * {@link NewsletterDeliveryWorker}.
     *
     * <p>The two conditions an admin needs answered immediately are checked synchronously here —
     * an empty subscriber list, and a missing email provider — so neither can hide behind an
     * optimistic {@code 200}. The per-recipient send itself is queued, because holding an HTTP
     * request open for one provider call per subscriber does not survive a growing list.
     *
     * <p>Callers follow the campaign to its terminal status via
     * {@code GET /admin/newsletter/campaigns/{id}}.
     *
     * @return the persisted campaign, either QUEUED for delivery or already NO_RECIPIENTS
     * @throws ApiException 502 NEWSLETTER_SEND_FAILED when no email provider is configured
     */
    public NewsletterCampaign publish(UUID adminUserId, NewsletterPublishRequest request) {
        String htmlBody = NewsletterHtmlBody.decode(request.getHtmlBody());
        int recipientCount = (int) subscriberRepository.countByStatusAndConsentTrue(STATUS_ACTIVE);

        if (recipientCount == 0) {
            // Saving a campaign nobody received is not a successful send. Record it as such so
            // the admin portal can warn instead of showing a bare 200.
            log.warn("Newsletter publish requested with no active consented subscribers");
            return campaignWriter.create(adminUserId, request, htmlBody, 0, CampaignStatus.NO_RECIPIENTS);
        }

        if (!emailService.isProviderConfigured()) {
            // Queueing a broadcast that provably cannot be delivered would just defer the failure
            // out of the admin's sight, so refuse it up front.
            NewsletterCampaign failed = campaignWriter.create(
                    adminUserId, request, htmlBody, recipientCount, CampaignStatus.FAILED);
            campaignWriter.finish(failed.getId(), 0, 0, CampaignStatus.FAILED,
                    "No email provider configured (set a SendGrid API key or SMTP credentials)");

            throw new ApiException(
                    "Newsletter cannot be sent: no email provider is configured on user-service",
                    HttpStatus.BAD_GATEWAY,
                    "NEWSLETTER_SEND_FAILED");
        }

        NewsletterCampaign campaign = campaignWriter.create(
                adminUserId, request, htmlBody, recipientCount, CampaignStatus.QUEUED);

        log.info("Newsletter campaign {} queued for delivery to {} subscriber(s)",
                campaign.getId(), recipientCount);

        return campaign;
    }

    @Transactional(readOnly = true)
    public Page<NewsletterCampaignResponse> getCampaigns(Pageable pageable) {
        return campaignRepository.findAllByDeletedFalse(pageable).map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public NewsletterCampaignDetailResponse getCampaign(UUID id) {
        NewsletterCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Newsletter campaign not found: " + id,
                        HttpStatus.NOT_FOUND,
                        "NEWSLETTER_CAMPAIGN_NOT_FOUND"));
        return toDetailResponse(campaign);
    }

    /** Subscribers who would receive a newsletter sent right now. */
    @Transactional(readOnly = true)
    public long countActiveSubscribers() {
        return subscriberRepository.countByStatusAndConsentTrue(STATUS_ACTIVE);
    }

    // --- internals ---

    private void markUnsubscribed(NewsletterSubscriber subscriber) {
        subscriber.setStatus(STATUS_UNSUBSCRIBED);
        subscriberRepository.save(subscriber);
        log.info("Newsletter unsubscribed for: {}", subscriber.getEmail());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private NewsletterCampaignResponse toSummaryResponse(NewsletterCampaign c) {
        return NewsletterCampaignResponse.builder()
                .id(c.getId())
                .subject(c.getSubject())
                .previewText(c.getPreviewText())
                .recipientCount(c.getRecipientCount())
                .successCount(c.getSuccessCount())
                .failureCount(c.getFailureCount())
                .status(c.getStatus())
                .errorMessage(c.getErrorMessage())
                .sentBy(c.getSentBy())
                .createdAt(c.getCreatedAt())
                .completedAt(c.getCompletedAt())
                .build();
    }

    private NewsletterCampaignDetailResponse toDetailResponse(NewsletterCampaign c) {
        return NewsletterCampaignDetailResponse.builder()
                .id(c.getId())
                .subject(c.getSubject())
                .htmlBody(c.getHtmlBody())
                .textBody(c.getTextBody())
                .previewText(c.getPreviewText())
                .recipientCount(c.getRecipientCount())
                .successCount(c.getSuccessCount())
                .failureCount(c.getFailureCount())
                .status(c.getStatus())
                .attemptCount(c.getAttemptCount())
                .errorMessage(c.getErrorMessage())
                .sentBy(c.getSentBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .startedAt(c.getStartedAt())
                .completedAt(c.getCompletedAt())
                .build();
    }
}
