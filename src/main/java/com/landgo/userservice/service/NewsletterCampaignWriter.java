package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.NewsletterPublishRequest;
import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.entity.NewsletterCampaignRecipient;
import com.landgo.userservice.enums.CampaignStatus;
import com.landgo.userservice.repository.NewsletterCampaignRecipientRepository;
import com.landgo.userservice.repository.NewsletterCampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Transactional writes for newsletter campaigns.
 *
 * <p>Separate from {@link NewsletterService} and {@link NewsletterSender} so no transaction is
 * held open across the email provider calls, and so a failure mid-broadcast cannot mark the
 * surrounding transaction rollback-only and discard the status update recording that failure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterCampaignWriter {

    private static final int MAX_ERROR_LENGTH = 1000;

    private final NewsletterCampaignRepository campaignRepository;
    private final NewsletterCampaignRecipientRepository recipientRepository;

    /** Persists the campaign in {@code QUEUED} for the delivery worker to pick up. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NewsletterCampaign create(UUID adminUserId, NewsletterPublishRequest request,
                                     String decodedHtmlBody, int recipientCount, String status) {
        NewsletterCampaign campaign = NewsletterCampaign.builder()
                .subject(request.getSubject())
                .htmlBody(decodedHtmlBody)
                .textBody(request.getTextBody())
                .previewText(request.getPreviewText())
                .recipientCount(recipientCount)
                .status(status)
                .sentBy(adminUserId)
                .build();

        if (!CampaignStatus.QUEUED.equals(status)) {
            campaign.setCompletedAt(LocalDateTime.now());
        }

        NewsletterCampaign saved = campaignRepository.save(campaign);
        log.info("Created newsletter campaign {} as {} targeting {} subscriber(s)",
                saved.getId(), status, recipientCount);
        return saved;
    }

    /**
     * Atomically claims a queued campaign by moving it to {@code PROCESSING}.
     *
     * @return the claimed campaign, or empty if another worker got there first
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<NewsletterCampaign> claim(UUID campaignId) {
        NewsletterCampaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null || !CampaignStatus.QUEUED.equals(campaign.getStatus())) {
            return Optional.empty();
        }

        campaign.setStatus(CampaignStatus.PROCESSING);
        campaign.setStartedAt(LocalDateTime.now());
        campaign.setAttemptCount(campaign.getAttemptCount() + 1);

        return Optional.of(campaignRepository.save(campaign));
    }

    /** Records the terminal status and real delivery counts once the broadcast finishes. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NewsletterCampaign finish(UUID campaignId, int successCount, int failureCount,
                                     String status, String error) {
        NewsletterCampaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            log.warn("Newsletter campaign {} disappeared before its result could be recorded", campaignId);
            return null;
        }

        campaign.setSuccessCount(successCount);
        campaign.setFailureCount(failureCount);
        campaign.setStatus(status);
        campaign.setCompletedAt(LocalDateTime.now());
        campaign.setErrorMessage(truncate(error));

        return campaignRepository.save(campaign);
    }

    /**
     * Requeues a campaign left in {@code PROCESSING}, or fails it once it has exhausted its
     * retries. A requeued campaign resumes from the subscribers it has not yet emailed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverStale(UUID campaignId, int maxAttempts) {
        NewsletterCampaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null || !CampaignStatus.PROCESSING.equals(campaign.getStatus())) {
            return;
        }

        long delivered = recipientRepository.countByCampaignIdAndStatus(campaignId, "SENT");

        if (campaign.getAttemptCount() >= maxAttempts) {
            log.error("Newsletter campaign {} stuck in PROCESSING after {} attempt(s); marking terminal "
                    + "({} of {} delivered)", campaignId, campaign.getAttemptCount(), delivered, campaign.getRecipientCount());

            // Mail that did go out still counts — report PARTIAL rather than erasing it as FAILED.
            int successCount = (int) delivered;
            int failureCount = Math.max(0, campaign.getRecipientCount() - successCount);
            campaign.setSuccessCount(successCount);
            campaign.setFailureCount(failureCount);
            campaign.setStatus(CampaignStatus.terminalFor(successCount, failureCount));
            campaign.setCompletedAt(LocalDateTime.now());
            campaign.setErrorMessage("Delivery did not complete after " + campaign.getAttemptCount() + " attempt(s)");
        } else {
            log.warn("Newsletter campaign {} stale in PROCESSING (attempt {}); requeueing to resume from {} delivered",
                    campaignId, campaign.getAttemptCount(), delivered);
            campaign.setStatus(CampaignStatus.QUEUED);
            campaign.setStartedAt(null);
        }

        campaignRepository.save(campaign);
    }

    /**
     * Subscribers this campaign has already been delivered to.
     *
     * <p>A resumed broadcast skips these, so recovering an interrupted run cannot email the same
     * newsletter twice.
     */
    @Transactional(readOnly = true)
    public List<UUID> findDeliveredSubscriberIds(UUID campaignId) {
        return recipientRepository.findDeliveredSubscriberIds(campaignId);
    }

    /**
     * Writes one audit row per recipient in its own transaction, so a later failure in the
     * broadcast cannot erase the record of mail that was already delivered.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRecipient(UUID campaignId, UUID subscriberId, String status, String errorMessage) {
        try {
            recipientRepository.save(NewsletterCampaignRecipient.builder()
                    .campaignId(campaignId)
                    .subscriberId(subscriberId)
                    .status(status)
                    .errorMessage(truncate(errorMessage))
                    .build());
        } catch (Exception e) {
            // Losing an audit row must not fail an email that was actually delivered.
            log.error("Failed to write recipient log for campaign {} subscriber {}", campaignId, subscriberId, e);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
    }
}
