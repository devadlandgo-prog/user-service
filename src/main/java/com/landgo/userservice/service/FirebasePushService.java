package com.landgo.userservice.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebasePushService {

    /**
     * Sends a multicast push notification to a batch of FCM tokens using Firebase Admin SDK.
     *
     * @param tokens   The list of FCM device tokens (maximum 500 per batch per Firebase limits).
     * @param title    The title of the push notification.
     * @param body     The body content of the push notification.
     * @param imageUrl Optional image URL for the push notification.
     * @param data     Optional key-value data payload (e.g. deep links).
     * @return List of tokens that are no longer valid and should be removed/deactivated from the database.
     */
    public List<String> sendMulticastPush(List<String> tokens, String title, String body, String imageUrl, Map<String, String> data) {
        List<String> invalidTokens = new ArrayList<>();

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens provided for push notification delivery");
            return invalidTokens;
        }

        if (tokens.size() > 500) {
            log.warn("Firebase multicast allows a max of 500 tokens per batch. Sending to the first 500.");
            tokens = tokens.subList(0, 500);
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

            MulticastMessage message = messageBuilder.build();
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("Successfully sent {} messages. Failed: {}", response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse sendResponse = responses.get(i);
                    if (!sendResponse.isSuccessful()) {
                        String errorCode = sendResponse.getException().getMessagingErrorCode().name();
                        log.warn("Failed to send push to token at index {}. Error: {}", i, errorCode);
                        
                        // Firebase errors indicating the token is no longer valid
                        if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode) || "SENDER_ID_MISMATCH".equals(errorCode)) {
                            invalidTokens.add(tokens.get(i));
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to send multicast push notification", e);
        }

        return invalidTokens;
    }
}
