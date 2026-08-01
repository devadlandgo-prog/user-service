package com.landgo.userservice.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebasePushService {

    /** Firebase rejects multicast messages carrying more than 500 tokens. */
    public static final int MAX_TOKENS_PER_BATCH = 500;

    /** FCM error codes meaning the token is permanently dead and should be deactivated. */
    private static final Set<String> DEAD_TOKEN_ERROR_CODES =
            Set.of("UNREGISTERED", "INVALID_ARGUMENT", "SENDER_ID_MISMATCH");

    /**
     * Whether the Firebase Admin SDK was initialized with usable service account credentials.
     * When false, no push can be delivered and callers should fail the campaign explicitly
     * rather than reporting a send that never happened.
     */
    public boolean isAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Sends one multicast push to a batch of FCM tokens.
     *
     * @param tokens   device tokens, at most {@link #MAX_TOKENS_PER_BATCH}
     * @param title    notification title
     * @param body     notification body
     * @param imageUrl optional rich notification image
     * @param data     optional data payload (deep link, campaign id, notification type)
     * @return per-token outcome, including the tokens Firebase reported as dead
     */
    public PushSendResult sendMulticastPush(List<String> tokens, String title, String body,
                                            String imageUrl, Map<String, String> data) {
        PushSendResult result = new PushSendResult();

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens provided for push notification delivery");
            return result;
        }

        if (!isAvailable()) {
            log.error("Firebase Admin SDK is not initialized; cannot deliver push to {} token(s)", tokens.size());
            return PushSendResult.allFailed(tokens.size(), "FIREBASE_NOT_CONFIGURED");
        }

        if (tokens.size() > MAX_TOKENS_PER_BATCH) {
            throw new IllegalArgumentException(
                    "Firebase multicast allows at most " + MAX_TOKENS_PER_BATCH + " tokens per batch, got " + tokens.size());
        }

        try {
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(title)
                    .setBody(body);

            if (imageUrl != null && !imageUrl.isBlank()) {
                notificationBuilder.setImage(imageUrl);
            }

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notificationBuilder.build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(messageBuilder.build());

            result.recordSuccess(response.getSuccessCount());
            result.recordFailure(response.getFailureCount());
            log.info("FCM multicast: {} succeeded, {} failed", response.getSuccessCount(), response.getFailureCount());

            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (sendResponse.isSuccessful()) {
                    continue;
                }

                String errorCode = resolveErrorCode(sendResponse);
                result.addErrorCode(errorCode);
                log.warn("Failed to send push to token at index {}. Error: {}", i, errorCode);

                if (DEAD_TOKEN_ERROR_CODES.contains(errorCode)) {
                    result.addInvalidToken(tokens.get(i));
                }
            }

            return result;

        } catch (Exception e) {
            // A throw here means the whole batch was rejected (auth, quota, transport). Report it
            // as a batch-wide failure so the campaign is never marked SENT on a send that failed.
            log.error("Failed to send multicast push notification to {} token(s)", tokens.size(), e);
            return PushSendResult.allFailed(tokens.size(), "FCM_SEND_EXCEPTION");
        }
    }

    private String resolveErrorCode(SendResponse sendResponse) {
        if (sendResponse.getException() == null) {
            return "UNKNOWN";
        }
        if (sendResponse.getException().getMessagingErrorCode() != null) {
            return sendResponse.getException().getMessagingErrorCode().name();
        }
        return sendResponse.getException().getErrorCode() != null
                ? sendResponse.getException().getErrorCode().name()
                : "UNKNOWN";
    }
}
