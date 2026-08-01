package com.landgo.userservice.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Outcome of one multicast send to FCM.
 *
 * <p>Carries the counts reported by Firebase rather than inferring them from the number of dead
 * tokens, so a batch that fails for reasons unrelated to token validity (quota, auth, network)
 * is still recorded as a failure.
 */
public class PushSendResult {

    private int successCount;
    private int failureCount;
    private final List<String> invalidTokens = new ArrayList<>();
    private final Set<String> errorCodes = new LinkedHashSet<>();

    public static PushSendResult allFailed(int tokenCount, String errorCode) {
        PushSendResult result = new PushSendResult();
        result.failureCount = tokenCount;
        result.errorCodes.add(errorCode);
        return result;
    }

    public void recordSuccess(int count) {
        this.successCount += count;
    }

    public void recordFailure(int count) {
        this.failureCount += count;
    }

    public void addInvalidToken(String token) {
        this.invalidTokens.add(token);
    }

    public void addErrorCode(String errorCode) {
        if (errorCode != null) {
            this.errorCodes.add(errorCode);
        }
    }

    public void merge(PushSendResult other) {
        this.successCount += other.successCount;
        this.failureCount += other.failureCount;
        this.invalidTokens.addAll(other.invalidTokens);
        this.errorCodes.addAll(other.errorCodes);
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public List<String> getInvalidTokens() {
        return invalidTokens;
    }

    public Set<String> getErrorCodes() {
        return errorCodes;
    }

    /** Compact summary of distinct FCM error codes, for persisting on a failed campaign. */
    public String errorSummary() {
        return errorCodes.isEmpty() ? null : String.join(", ", errorCodes);
    }
}
