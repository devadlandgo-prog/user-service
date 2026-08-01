package com.landgo.userservice.enums;

/**
 * Delivery lifecycle shared by push campaigns and newsletter campaigns.
 *
 * <p>Stored as a plain string column so historical rows written before this enum existed still
 * deserialize. Terminal states are {@link #SENT}, {@link #PARTIAL}, {@link #FAILED} and
 * {@link #NO_RECIPIENTS}.
 */
public final class CampaignStatus {

    /** Saved but not submitted for delivery. */
    public static final String DRAFT = "DRAFT";

    /** Accepted and waiting for the delivery worker to pick it up. */
    public static final String QUEUED = "QUEUED";

    /** Waiting for its scheduledAt timestamp to arrive. */
    public static final String SCHEDULED = "SCHEDULED";

    /** Claimed by a worker that is currently calling the delivery provider. */
    public static final String PROCESSING = "PROCESSING";

    /** Every targeted recipient was accepted by the provider. */
    public static final String SENT = "SENT";

    /** Some recipients succeeded and some failed. */
    public static final String PARTIAL = "PARTIAL";

    /** All sends failed, or the provider itself errored. */
    public static final String FAILED = "FAILED";

    /** Nothing matched the audience — no active device tokens or subscribers. */
    public static final String NO_RECIPIENTS = "NO_RECIPIENTS";

    private CampaignStatus() {
    }

    /**
     * Chooses the terminal status for a completed delivery run.
     *
     * @param successCount recipients the provider accepted
     * @param failureCount recipients the provider rejected
     */
    public static String terminalFor(int successCount, int failureCount) {
        if (successCount == 0 && failureCount == 0) {
            return NO_RECIPIENTS;
        }
        if (failureCount == 0) {
            return SENT;
        }
        return successCount == 0 ? FAILED : PARTIAL;
    }
}
