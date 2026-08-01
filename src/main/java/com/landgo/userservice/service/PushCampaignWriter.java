package com.landgo.userservice.service;

import com.landgo.userservice.entity.PushCampaign;
import com.landgo.userservice.entity.PushDeliveryLog;
import com.landgo.userservice.enums.CampaignStatus;
import com.landgo.userservice.repository.PushCampaignRepository;
import com.landgo.userservice.repository.PushDeliveryLogRepository;
import com.landgo.userservice.repository.UserDeviceTokenRepository;
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
 * Transactional writes for push campaign delivery.
 *
 * <p>Kept separate from {@link PushCampaignDispatcher} so that no transaction is held open across
 * the FCM network calls, and so a failure while sending cannot mark the surrounding transaction
 * rollback-only and discard the very status update that records the failure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushCampaignWriter {

    /** error_message is TEXT, but an unbounded provider error still has no business being stored. */
    private static final int MAX_ERROR_LENGTH = 1000;

    /** error_code is VARCHAR(64) in the schema. */
    private static final int MAX_ERROR_CODE_LENGTH = 64;

    private final PushCampaignRepository pushCampaignRepository;
    private final PushDeliveryLogRepository pushDeliveryLogRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    /**
     * Atomically claims a dispatchable campaign by moving it to {@code PROCESSING}.
     *
     * @return the claimed campaign, or empty if it was already claimed or is no longer dispatchable
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<PushCampaign> claim(UUID campaignId) {
        PushCampaign campaign = pushCampaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            return Optional.empty();
        }

        if (!CampaignStatus.QUEUED.equals(campaign.getStatus())
                && !CampaignStatus.SCHEDULED.equals(campaign.getStatus())) {
            return Optional.empty();
        }

        campaign.setStatus(CampaignStatus.PROCESSING);
        campaign.setStartedAt(LocalDateTime.now());
        campaign.setAttemptCount(campaign.getAttemptCount() == null ? 1 : campaign.getAttemptCount() + 1);

        return Optional.of(pushCampaignRepository.save(campaign));
    }

    /** Records the terminal status, delivery counts and any error summary. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(UUID campaignId, int targetedCount, int successCount, int failureCount,
                       String status, String error) {
        PushCampaign campaign = pushCampaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            log.warn("Campaign {} disappeared before its result could be recorded", campaignId);
            return;
        }

        campaign.setTargetedCount(targetedCount);
        campaign.setSuccessCount(successCount);
        campaign.setFailureCount(failureCount);
        campaign.setStatus(status);
        campaign.setCompletedAt(LocalDateTime.now());
        campaign.setErrorMessage(truncate(error, MAX_ERROR_LENGTH));

        pushCampaignRepository.save(campaign);
    }

    /**
     * Requeues a campaign left in {@code PROCESSING}, or fails it once it has exhausted its
     * retries, so nothing sits in a non-terminal state indefinitely.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverStale(UUID campaignId, int maxAttempts) {
        PushCampaign campaign = pushCampaignRepository.findById(campaignId).orElse(null);
        if (campaign == null || !CampaignStatus.PROCESSING.equals(campaign.getStatus())) {
            return;
        }

        int attempts = campaign.getAttemptCount() == null ? 0 : campaign.getAttemptCount();
        if (attempts >= maxAttempts) {
            log.error("Campaign {} stuck in PROCESSING after {} attempt(s); marking FAILED", campaignId, attempts);
            campaign.setStatus(CampaignStatus.FAILED);
            campaign.setCompletedAt(LocalDateTime.now());
            campaign.setErrorMessage("Delivery did not complete after " + attempts + " attempt(s)");
        } else {
            log.warn("Campaign {} stale in PROCESSING (attempt {}); requeueing", campaignId, attempts);
            campaign.setStatus(CampaignStatus.QUEUED);
            campaign.setStartedAt(null);
        }

        pushCampaignRepository.save(campaign);
    }

    /** Persists one audit row per rejected recipient so FCM error codes stay reviewable. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDeliveryLogs(List<PushDeliveryLog> logs) {
        if (logs.isEmpty()) {
            return;
        }

        try {
            logs.forEach(entry -> entry.setErrorCode(truncate(entry.getErrorCode(), MAX_ERROR_CODE_LENGTH)));
            pushDeliveryLogRepository.saveAll(logs);
        } catch (Exception e) {
            // Losing audit rows must not fail a campaign whose pushes were actually delivered.
            log.error("Failed to write {} push delivery log(s)", logs.size(), e);
        }
    }

    /** Deactivates tokens FCM reported as expired or unregistered. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateTokens(List<String> tokens) {
        if (tokens.isEmpty()) {
            return;
        }

        userDeviceTokenRepository.deactivateTokens(tokens);
        log.info("Deactivated {} invalid FCM token(s)", tokens.size());
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
