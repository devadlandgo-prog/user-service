package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.NewsletterPublishRequest;
import com.landgo.userservice.dto.request.NewsletterSubscribeRequest;
import com.landgo.userservice.dto.response.NewsletterSubscribeResponse;
import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.entity.NewsletterCampaignRecipient;
import com.landgo.userservice.entity.NewsletterSubscriber;
import com.landgo.userservice.repository.NewsletterCampaignRecipientRepository;
import com.landgo.userservice.repository.NewsletterCampaignRepository;
import com.landgo.userservice.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterCampaignRepository campaignRepository;
    private final NewsletterCampaignRecipientRepository campaignRecipientRepository;
    private final EmailService emailService;

    @Transactional
    public NewsletterSubscribeResponse subscribe(NewsletterSubscribeRequest request) {
        Optional<NewsletterSubscriber> existingOpt = subscriberRepository.findByEmail(request.getEmail());
        
        NewsletterSubscriber subscriber;
        if (existingOpt.isPresent()) {
            subscriber = existingOpt.get();
            if ("ACTIVE".equals(subscriber.getStatus())) {
                return NewsletterSubscribeResponse.builder()
                        .success(true)
                        .subscriberId(subscriber.getId())
                        .message("Already subscribed")
                        .build();
            }
            subscriber.setStatus("ACTIVE");
            subscriber.setConsent(request.isConsent());
        } else {
            subscriber = NewsletterSubscriber.builder()
                    .email(request.getEmail())
                    .consent(request.isConsent())
                    .status("ACTIVE")
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
        Optional<NewsletterSubscriber> existingOpt = subscriberRepository.findByEmail(email);
        if (existingOpt.isPresent()) {
            NewsletterSubscriber subscriber = existingOpt.get();
            subscriber.setStatus("UNSUBSCRIBED");
            subscriberRepository.save(subscriber);
            log.info("Newsletter unsubscribed for: {}", email);
        }
    }

    @Transactional
    public NewsletterCampaign initiatePublish(UUID adminUserId, NewsletterPublishRequest request) {
        List<NewsletterSubscriber> activeSubscribers = subscriberRepository.findByStatus("ACTIVE");

        String htmlBody = request.getHtmlBody();
        if (htmlBody != null && !htmlBody.isBlank()) {
            try {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(htmlBody.trim());
                String decoded = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
                if (decoded.contains("<") || decoded.contains(">")) {
                    htmlBody = decoded;
                }
            } catch (IllegalArgumentException e) {
                // Keep original if not valid base64
            }
        }

        NewsletterCampaign campaign = NewsletterCampaign.builder()
                .subject(request.getSubject())
                .htmlBody(htmlBody)
                .textBody(request.getTextBody())
                .previewText(request.getPreviewText())
                .recipientCount(activeSubscribers.size())
                .sentBy(adminUserId)
                .build();

        campaign = campaignRepository.save(campaign);
        
        // Trigger direct async send
        sendCampaignAsynchronously(campaign, activeSubscribers);
        
        return campaign;
    }

    @Async
    public void sendCampaignAsynchronously(NewsletterCampaign campaign, List<NewsletterSubscriber> subscribers) {
        log.info("Starting asynchronous broadcast for newsletter campaign: {}", campaign.getId());
        
        int successCount = 0;
        int failureCount = 0;

        for (NewsletterSubscriber sub : subscribers) {
            NewsletterCampaignRecipient logEntry = NewsletterCampaignRecipient.builder()
                    .campaignId(campaign.getId())
                    .subscriberId(sub.getId())
                    .build();
            
            try {
                emailService.sendDynamicHtmlEmail(sub.getEmail(), campaign.getSubject(), campaign.getHtmlBody());
                logEntry.setStatus("SENT");
                successCount++;
            } catch (Exception e) {
                log.error("Failed to deliver campaign {} to subscriber {}", campaign.getId(), sub.getEmail(), e);
                logEntry.setStatus("FAILED");
                logEntry.setErrorMessage(e.getMessage());
                failureCount++;
            }
            
            try {
                campaignRecipientRepository.save(logEntry);
            } catch (Exception dbEx) {
                log.error("Failed to write recipient log for sub: {}", sub.getEmail(), dbEx);
            }
        }

        log.info("Broadcast complete for campaign {}. Successes: {}, Failures: {}", campaign.getId(), successCount, failureCount);
    }
}
