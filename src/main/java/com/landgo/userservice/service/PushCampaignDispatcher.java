package com.landgo.userservice.service;

import com.landgo.userservice.entity.PushCampaign;
import com.landgo.userservice.entity.PushDeliveryLog;
import com.landgo.userservice.entity.UserDeviceToken;
import com.landgo.userservice.enums.CampaignStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Sends a single push campaign and records its outcome.
 *
 * <p>Deliberately not transactional: delivery involves FCM network calls that can take seconds,
 * and every database write is delegated to {@link PushCampaignWriter}, which commits each one in
 * its own transaction. That way a mid-send failure still leaves a durable record of what was
 * already delivered, and one campaign's failure cannot roll back another's status update.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushCampaignDispatcher {

    private final PushCampaignWriter writer;
    private final FirebasePushService firebasePushService;
    private final PushAudienceResolver audienceResolver;

    /** Claims a campaign for this worker; empty when another worker already took it. */
    public Optional<PushCampaign> claim(UUID campaignId) {
        return writer.claim(campaignId);
    }

    /**
     * Delivers a claimed campaign to every device matching its audience, then writes the terminal
     * status, delivery counts and per-recipient logs.
     */
    public void dispatch(PushCampaign campaign) {
        UUID campaignId = campaign.getId();

        try {
            List<UserDeviceToken> devices =
                    audienceResolver.resolve(campaign.getAudience(), campaign.getAudienceFilter());

            if (devices.isEmpty()) {
                // An empty audience is a real outcome, not a job still waiting to run.
                log.info("Campaign {} matched no active device tokens", campaignId);
                writer.finish(campaignId, 0, 0, 0, CampaignStatus.NO_RECIPIENTS, null);
                return;
            }

            Map<String, String> data = buildDataPayload(campaign);
            PushSendResult total = new PushSendResult();

            for (int i = 0; i < devices.size(); i += FirebasePushService.MAX_TOKENS_PER_BATCH) {
                List<UserDeviceToken> batch = devices.subList(
                        i, Math.min(devices.size(), i + FirebasePushService.MAX_TOKENS_PER_BATCH));

                PushSendResult batchResult = firebasePushService.sendMulticastPush(
                        batch.stream().map(UserDeviceToken::getFcmToken).toList(),
                        campaign.getTitle(),
                        campaign.getBody(),
                        campaign.getImageUrl(),
                        data);

                total.merge(batchResult);
                writer.saveDeliveryLogs(buildDeliveryLogs(campaignId, batch, batchResult));
            }

            writer.deactivateTokens(total.getInvalidTokens());

            String status = CampaignStatus.terminalFor(total.getSuccessCount(), total.getFailureCount());
            writer.finish(campaignId, devices.size(), total.getSuccessCount(), total.getFailureCount(),
                    status, total.errorSummary());

            log.info("Campaign {} finished as {}: {} succeeded, {} failed",
                    campaignId, status, total.getSuccessCount(), total.getFailureCount());

        } catch (Exception e) {
            log.error("Failed to dispatch push campaign {}", campaignId, e);
            writer.finish(campaignId, campaign.getTargetedCount(), 0, 0, CampaignStatus.FAILED,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Requeues or fails a campaign whose worker never finished. */
    public void recoverStale(UUID campaignId, int maxAttempts) {
        writer.recoverStale(campaignId, maxAttempts);
    }

    // --- internals ---

    private Map<String, String> buildDataPayload(PushCampaign campaign) {
        Map<String, String> data = new HashMap<>();
        data.put("campaignId", campaign.getId().toString());
        data.put("type", "CAMPAIGN");
        if (campaign.getDeepLink() != null && !campaign.getDeepLink().isBlank()) {
            data.put("deepLink", campaign.getDeepLink());
        }
        return data;
    }

    private List<PushDeliveryLog> buildDeliveryLogs(UUID campaignId, List<UserDeviceToken> batch,
                                                    PushSendResult result) {
        List<String> invalid = result.getInvalidTokens();
        if (invalid.isEmpty()) {
            return List.of();
        }

        String errorCode = result.errorSummary();
        List<PushDeliveryLog> logs = new ArrayList<>();

        for (UserDeviceToken device : batch) {
            if (!invalid.contains(device.getFcmToken())) {
                continue;
            }
            logs.add(PushDeliveryLog.builder()
                    .campaignId(campaignId)
                    .userId(device.getUserId())
                    .fcmToken(device.getFcmToken())
                    .status("INVALID_TOKEN")
                    .errorCode(errorCode)
                    .build());
        }

        return logs;
    }
}
