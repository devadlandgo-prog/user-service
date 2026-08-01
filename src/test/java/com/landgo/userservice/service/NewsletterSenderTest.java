package com.landgo.userservice.service;

import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.entity.NewsletterSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uses hand-written test doubles rather than a mocking framework: these collaborators are
 * concrete classes, and inline class mocking fails on JDKs newer than the one the project's
 * Mockito supports. Subclassing keeps the test runnable on any JDK.
 */
class NewsletterSenderTest {

    private FakeEmailService emailService;
    private FakeCampaignWriter campaignWriter;
    private NewsletterSender newsletterSender;
    private NewsletterCampaign campaign;

    @BeforeEach
    void setUp() {
        emailService = new FakeEmailService();
        campaignWriter = new FakeCampaignWriter();
        newsletterSender = new NewsletterSender(emailService, campaignWriter);

        ReflectionTestUtils.setField(newsletterSender, "unsubscribeBaseUrl",
                "https://landgo.ca/newsletter/unsubscribe");

        campaign = NewsletterCampaign.builder()
                .subject("Weekend land deals")
                .htmlBody("<h1>Hello</h1>")
                .build();
        campaign.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("emails every subscriber and counts them as successes")
    void sendsToAllSubscribers() {
        List<NewsletterSubscriber> subscribers = List.of(subscriber("a@example.com"), subscriber("b@example.com"));

        NewsletterSender.NewsletterSendResult result = newsletterSender.send(campaign, subscribers);

        assertEquals(2, result.successCount());
        assertEquals(0, result.failureCount());
        assertEquals(List.of("a@example.com", "b@example.com"), emailService.recipients());
    }

    @Test
    @DisplayName("counts a provider failure without aborting the rest of the broadcast")
    void oneFailureDoesNotStopTheRun() {
        NewsletterSubscriber bad = subscriber("bad@example.com");
        NewsletterSubscriber good = subscriber("good@example.com");
        emailService.failFor("bad@example.com", "550 rejected");

        NewsletterSender.NewsletterSendResult result = newsletterSender.send(campaign, List.of(bad, good));

        assertEquals(1, result.successCount());
        assertEquals(1, result.failureCount());
        assertEquals("550 rejected", result.lastError());
        // The run continued past the rejection.
        assertTrue(emailService.recipients().contains("good@example.com"));
        assertTrue(campaignWriter.recorded.contains(bad.getId() + ":FAILED"));
        assertTrue(campaignWriter.recorded.contains(good.getId() + ":SENT"));
    }

    @Test
    @DisplayName("a resumed campaign skips subscribers who were already emailed")
    void resumeDoesNotSendTwice() {
        NewsletterSubscriber alreadySent = subscriber("sent@example.com");
        NewsletterSubscriber pending = subscriber("pending@example.com");
        campaignWriter.delivered.add(alreadySent.getId());

        NewsletterSender.NewsletterSendResult result =
                newsletterSender.send(campaign, List.of(alreadySent, pending));

        assertFalse(emailService.recipients().contains("sent@example.com"));
        assertTrue(emailService.recipients().contains("pending@example.com"));
        // The previously delivered recipient still counts toward the campaign total.
        assertEquals(2, result.successCount());
        assertEquals(0, result.failureCount());
    }

    @Test
    @DisplayName("every email carries a tokenised unsubscribe link")
    void appendsUnsubscribeLink() {
        NewsletterSubscriber subscriber = subscriber("a@example.com");

        newsletterSender.send(campaign, List.of(subscriber));

        String html = emailService.sent.get(0).html();
        assertTrue(html.startsWith("<h1>Hello</h1>"));
        assertTrue(html.contains("token=" + subscriber.getUnsubscribeToken()));
        assertTrue(html.contains("Unsubscribe"));
    }

    private NewsletterSubscriber subscriber(String email) {
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(email)
                .consent(true)
                .status("ACTIVE")
                .unsubscribeToken(UUID.randomUUID())
                .build();
        subscriber.setId(UUID.randomUUID());
        return subscriber;
    }

    // --- test doubles ---

    private record SentEmail(String to, String subject, String html) {
    }

    private static class FakeEmailService extends EmailService {
        private final List<SentEmail> sent = new ArrayList<>();
        private String failingRecipient;
        private String failureMessage;

        FakeEmailService() {
            super(null, null);
        }

        void failFor(String recipient, String message) {
            this.failingRecipient = recipient;
            this.failureMessage = message;
        }

        List<String> recipients() {
            return sent.stream().map(SentEmail::to).toList();
        }

        @Override
        public void sendHtmlEmailSync(String toEmail, String subject, String htmlContent) {
            if (toEmail.equals(failingRecipient)) {
                throw new RuntimeException(failureMessage);
            }
            sent.add(new SentEmail(toEmail, subject, htmlContent));
        }
    }

    private static class FakeCampaignWriter extends NewsletterCampaignWriter {
        private final List<UUID> delivered = new ArrayList<>();
        private final List<String> recorded = new ArrayList<>();

        FakeCampaignWriter() {
            super(null, null);
        }

        @Override
        public List<UUID> findDeliveredSubscriberIds(UUID campaignId) {
            return delivered;
        }

        @Override
        public void recordRecipient(UUID campaignId, UUID subscriberId, String status, String errorMessage) {
            recorded.add(subscriberId + ":" + status);
        }
    }
}
