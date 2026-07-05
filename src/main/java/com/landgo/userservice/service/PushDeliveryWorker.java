package com.landgo.userservice.service;

import com.landgo.userservice.entity.PushCampaign;
import com.landgo.userservice.repository.PushCampaignRepository;
import com.landgo.userservice.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushDeliveryWorker {

    private final PushCampaignRepository pushCampaignRepository;
    private final PushCampaignService pushCampaignService;
    private final FirebasePushService firebasePushService;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void processQueuedCampaigns() {
        List<PushCampaign> queuedCampaigns = pushCampaignRepository.findByStatus("QUEUED");

        for (PushCampaign campaign : queuedCampaigns) {
            // Check if it's scheduled for the future
            if (campaign.getScheduledAt() != null && campaign.getScheduledAt().isAfter(LocalDateTime.now())) {
                continue; // Not time yet
            }

            log.info("Processing push campaign: {}", campaign.getId());
            campaign.setStatus("SENDING");
            campaign.setStartedAt(LocalDateTime.now());
            pushCampaignRepository.save(campaign);

            try {
                List<String> tokens = pushCampaignService.resolveTokensForAudience(campaign.getAudience(), campaign.getAudienceFilter());
                
                campaign.setTargetedCount(tokens.size());

                if (tokens.isEmpty()) {
                    log.info("Campaign {} has 0 targeted tokens. Marking as SENT.", campaign.getId());
                    campaign.setStatus("SENT");
                    campaign.setCompletedAt(LocalDateTime.now());
                    pushCampaignRepository.save(campaign);
                    continue;
                }

                Map<String, String> data = new HashMap<>();
                if (campaign.getDeepLink() != null) {
                    data.put("deepLink", campaign.getDeepLink());
                }
                data.put("campaignId", campaign.getId().toString());

                // Firebase allows max 500 tokens per multicast message
                int successCount = 0;
                int failureCount = 0;

                for (int i = 0; i < tokens.size(); i += 500) {
                    List<String> batch = tokens.subList(i, Math.min(tokens.size(), i + 500));
                    List<String> invalidTokens = firebasePushService.sendMulticastPush(
                            batch,
                            campaign.getTitle(),
                            campaign.getBody(),
                            campaign.getImageUrl(),
                            data
                    );

                    // We assume success = batch.size() - invalidTokens.size() for simplicity here,
                    // or better, if FirebasePushService returned exact success count it would be ideal.
                    // For now, any invalid token means failure. Other failures are also possible, 
                    // but we track known dead tokens to deactivate them.
                    int failedInBatch = invalidTokens.size(); 
                    successCount += (batch.size() - failedInBatch);
                    failureCount += failedInBatch;

                    if (!invalidTokens.isEmpty()) {
                        userDeviceTokenRepository.deactivateTokens(invalidTokens);
                        log.info("Deactivated {} invalid tokens from batch", invalidTokens.size());
                    }
                }

                campaign.setSuccessCount(successCount);
                campaign.setFailureCount(failureCount);
                campaign.setStatus("SENT");
                campaign.setCompletedAt(LocalDateTime.now());
                pushCampaignRepository.save(campaign);
                log.info("Completed push campaign {}: {} successes, {} failures", campaign.getId(), successCount, failureCount);

            } catch (Exception e) {
                log.error("Failed to process push campaign {}", campaign.getId(), e);
                campaign.setStatus("FAILED");
                campaign.setCompletedAt(LocalDateTime.now());
                pushCampaignRepository.save(campaign);
            }
        }
    }
}
