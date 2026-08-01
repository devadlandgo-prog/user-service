package com.landgo.userservice.service;

import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.entity.NewsletterSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Delivers a newsletter to each subscriber and records the per-recipient outcome.
 *
 * <p>Runs on the delivery worker's thread, never on the publish request thread — a broadcast to a
 * large list would otherwise hold an HTTP connection open for the length of the send.
 *
 * <p>Every message carries a personalised unsubscribe link, which is a legal requirement for bulk
 * commercial email (CASL/CAN-SPAM) and keeps LandGo's sending domain out of spam folders.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterSender {

    private final EmailService emailService;
    private final NewsletterCampaignWriter campaignWriter;

    @Value("${app.newsletter.unsubscribe-url:https://landgo.ca/newsletter/unsubscribe}")
    private String unsubscribeBaseUrl;

    /**
     * Sends the campaign to every supplied subscriber who has not already received it.
     *
     * <p>Individual failures are recorded and counted rather than aborting the run, so one bad
     * address cannot stop the broadcast. Subscribers already marked {@code SENT} for this campaign
     * are skipped, which makes a retry after a crash safe to run.
     *
     * @return the aggregate outcome, counting previously delivered recipients as successes
     */
    public NewsletterSendResult send(NewsletterCampaign campaign, List<NewsletterSubscriber> subscribers) {
        Set<UUID> alreadyDelivered = new HashSet<>(
                campaignWriter.findDeliveredSubscriberIds(campaign.getId()));

        if (!alreadyDelivered.isEmpty()) {
            log.info("Resuming campaign {}: skipping {} subscriber(s) already emailed",
                    campaign.getId(), alreadyDelivered.size());
        }

        log.info("Broadcasting newsletter campaign {} to {} subscriber(s)",
                campaign.getId(), subscribers.size() - alreadyDelivered.size());

        int successCount = alreadyDelivered.size();
        int failureCount = 0;
        String lastError = null;

        for (NewsletterSubscriber subscriber : subscribers) {
            if (alreadyDelivered.contains(subscriber.getId())) {
                continue;
            }

            String status;
            String errorMessage = null;

            try {
                String html = appendUnsubscribeFooter(campaign.getHtmlBody(), subscriber);
                emailService.sendHtmlEmailSync(subscriber.getEmail(), campaign.getSubject(), html);
                status = "SENT";
                successCount++;
            } catch (Exception e) {
                log.error("Failed to deliver campaign {} to {}", campaign.getId(), subscriber.getEmail(), e);
                status = "FAILED";
                errorMessage = e.getMessage();
                lastError = e.getMessage();
                failureCount++;
            }

            campaignWriter.recordRecipient(campaign.getId(), subscriber.getId(), status, errorMessage);
        }

        log.info("Campaign {} broadcast complete: {} succeeded, {} failed",
                campaign.getId(), successCount, failureCount);

        return new NewsletterSendResult(successCount, failureCount, lastError);
    }

    /** Appends the unsubscribe footer required on every bulk send. */
    private String appendUnsubscribeFooter(String htmlBody, NewsletterSubscriber subscriber) {
        String link = unsubscribeBaseUrl
                + (unsubscribeBaseUrl.contains("?") ? "&" : "?")
                + "token=" + URLEncoder.encode(String.valueOf(subscriber.getUnsubscribeToken()), StandardCharsets.UTF_8);

        return htmlBody
                + "<hr style=\"margin-top:32px;border:none;border-top:1px solid #e5e7eb;\" />"
                + "<p style=\"font-size:12px;color:#6b7280;text-align:center;margin-top:16px;\">"
                + "You are receiving this because you subscribed to LandGo updates.<br />"
                + "<a href=\"" + link + "\" style=\"color:#6b7280;\">Unsubscribe</a>"
                + "</p>";
    }

    /** Aggregate outcome of one newsletter broadcast. */
    public record NewsletterSendResult(int successCount, int failureCount, String lastError) {
    }
}
