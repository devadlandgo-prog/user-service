package com.landgo.userservice.service;

import com.landgo.userservice.entity.PushCampaign;
import com.landgo.userservice.repository.PushCampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls for push campaigns awaiting delivery and hands each one to {@link PushCampaignDispatcher}.
 *
 * <p>Requires {@code @EnableScheduling} on the application class — without it this bean is
 * constructed but never invoked, which is why campaigns previously persisted as {@code QUEUED}
 * and stayed there indefinitely.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushDeliveryWorker {

    private final PushCampaignRepository pushCampaignRepository;
    private final PushCampaignDispatcher dispatcher;

    /** Campaigns handled per tick, bounding how long one run can take. */
    @Value("${app.push.worker.batch-size:25}")
    private int batchSize;

    /** A campaign in PROCESSING longer than this is treated as abandoned. */
    @Value("${app.push.worker.stale-after-minutes:5}")
    private int staleAfterMinutes;

    /** Total delivery attempts before a stuck campaign is marked FAILED. */
    @Value("${app.push.worker.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${app.push.worker.interval-ms:15000}")
    public void processQueuedCampaigns() {
        List<PushCampaign> dispatchable = pushCampaignRepository.findDispatchable(
                LocalDateTime.now(), PageRequest.of(0, batchSize));

        if (dispatchable.isEmpty()) {
            return;
        }

        log.info("Push delivery worker picked up {} campaign(s)", dispatchable.size());

        for (PushCampaign campaign : dispatchable) {
            // Claim and dispatch commit separately so a crash mid-send leaves the campaign in
            // PROCESSING for the stale sweep to recover, rather than silently back in QUEUED.
            dispatcher.claim(campaign.getId()).ifPresent(dispatcher::dispatch);
        }
    }

    /**
     * Sweeps campaigns whose worker never finished, so nothing stays in a non-terminal state.
     */
    @Scheduled(fixedDelayString = "${app.push.worker.recovery-interval-ms:60000}")
    public void recoverStaleCampaigns() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(staleAfterMinutes);
        List<PushCampaign> stale = pushCampaignRepository.findStaleProcessing(cutoff);

        if (stale.isEmpty()) {
            return;
        }

        log.warn("Recovering {} stale push campaign(s) stuck in PROCESSING", stale.size());
        for (PushCampaign campaign : stale) {
            dispatcher.recoverStale(campaign.getId(), maxAttempts);
        }
    }
}
