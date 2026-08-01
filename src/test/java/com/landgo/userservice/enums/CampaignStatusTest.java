package com.landgo.userservice.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CampaignStatusTest {

    @Test
    @DisplayName("all sends succeeding resolves to SENT")
    void allSuccessesAreSent() {
        assertEquals(CampaignStatus.SENT, CampaignStatus.terminalFor(1198, 0));
    }

    @Test
    @DisplayName("a mix of successes and failures resolves to PARTIAL")
    void mixedOutcomeIsPartial() {
        assertEquals(CampaignStatus.PARTIAL, CampaignStatus.terminalFor(1198, 42));
    }

    @Test
    @DisplayName("every send failing resolves to FAILED, never SENT")
    void allFailuresAreFailed() {
        assertEquals(CampaignStatus.FAILED, CampaignStatus.terminalFor(0, 42));
    }

    @Test
    @DisplayName("an empty audience resolves to NO_RECIPIENTS rather than staying queued")
    void emptyAudienceIsNoRecipients() {
        assertEquals(CampaignStatus.NO_RECIPIENTS, CampaignStatus.terminalFor(0, 0));
    }
}
