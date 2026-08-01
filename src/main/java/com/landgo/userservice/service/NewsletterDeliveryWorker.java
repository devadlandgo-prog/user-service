package com.landgo.userservice.service;

import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.entity.NewsletterSubscriber;
import com.landgo.userservice.enums.CampaignStatus;
import com.landgo.userservice.repository.NewsletterCampaignRepository;
import com.landgo.userservice.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls for queued newsletter campaigns and broadcasts them off the request thread.
 *
 * <p>Publishing only enqueues; this worker does the sending, so the HTTP request is not held open
 * for the length of a broadcast. The admin portal follows a campaign to its terminal state via
 * {@code GET /admin/newsletter/campaigns/{id}}, the same polling pattern used for push campaigns.
 *
 * <p>Not transactional: sends are network calls, and every database write is delegated to
 * {@link NewsletterCampaignWriter}, which commits each one independently.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterDeliveryWorker {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final NewsletterCampaignRepository campaignRepository;
    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterCampaignWriter campaignWriter;
    private final NewsletterSender newsletterSender;

    /** Campaigns handled per tick. Broadcasts are long, so one at a time is the sane default. */
    @Value("${app.newsletter.worker.batch-size:1}")
    private int batchSize;

    /** A campaign in PROCESSING longer than this is treated as abandoned. */
    @Value("${app.newsletter.worker.stale-after-minutes:30}")
    private int staleAfterMinutes;

    /** Total delivery attempts before a stuck campaign is finalised. */
    @Value("${app.newsletter.worker.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${app.newsletter.worker.interval-ms:15000}")
    public void processQueuedCampaigns() {
        List<NewsletterCampaign> dispatchable =
                campaignRepository.findDispatchable(PageRequest.of(0, batchSize));

        if (dispatchable.isEmpty()) {
            return;
        }

        log.info("Newsletter delivery worker picked up {} campaign(s)", dispatchable.size());

        for (NewsletterCampaign queued : dispatchable) {
            campaignWriter.claim(queued.getId()).ifPresent(this::deliver);
        }
    }

    /**
     * Sweeps campaigns whose worker never finished, so nothing stays in a non-terminal state.
     * A requeued campaign resumes from the subscribers it has not yet emailed.
     */
    @Scheduled(fixedDelayString = "${app.newsletter.worker.recovery-interval-ms:300000}")
    public void recoverStaleCampaigns() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(staleAfterMinutes);
        List<NewsletterCampaign> stale = campaignRepository.findStaleProcessing(cutoff);

        if (stale.isEmpty()) {
            return;
        }

        log.warn("Recovering {} stale newsletter campaign(s) stuck in PROCESSING", stale.size());
        for (NewsletterCampaign campaign : stale) {
            campaignWriter.recoverStale(campaign.getId(), maxAttempts);
        }
    }

    private void deliver(NewsletterCampaign campaign) {
        try {
            List<NewsletterSubscriber> recipients =
                    subscriberRepository.findByStatusAndConsentTrue(STATUS_ACTIVE);

            if (recipients.isEmpty()) {
                // Everyone unsubscribed between publish and delivery.
                log.warn("Newsletter campaign {} has no active consented subscribers at send time",
                        campaign.getId());
                campaignWriter.finish(campaign.getId(), 0, 0, CampaignStatus.NO_RECIPIENTS, null);
                return;
            }

            NewsletterSender.NewsletterSendResult result = newsletterSender.send(campaign, recipients);
            String status = CampaignStatus.terminalFor(result.successCount(), result.failureCount());

            campaignWriter.finish(campaign.getId(), result.successCount(), result.failureCount(),
                    status, result.lastError());

            log.info("Newsletter campaign {} finished as {}: {} succeeded, {} failed",
                    campaign.getId(), status, result.successCount(), result.failureCount());

        } catch (Exception e) {
            log.error("Failed to deliver newsletter campaign {}", campaign.getId(), e);
            campaignWriter.finish(campaign.getId(), 0, campaign.getRecipientCount(),
                    CampaignStatus.FAILED, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
