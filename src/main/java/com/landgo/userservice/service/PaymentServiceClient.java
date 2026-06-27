package com.landgo.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.payment-service-url:http://localhost:8082}")
    private String paymentServiceUrl;

    public boolean hasActiveSubscription(UUID userId, String type) {
        try {
            String url = paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/active";
            if (type != null) {
                url += "?type=" + type;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("active"));
        } catch (RestClientException e) {
            log.warn("payment-service subscription check unavailable for {}/type={}: {}", userId, type, e.getMessage());
            return false;
        }
    }

    /**
     * @return {@code 1} if the user has at least one active subscription, else {@code 0}.
     */
    public long countActiveSubscriptions(UUID userId) {
        return hasActiveSubscription(userId, null) ? 1 : 0;
    }

    /**
     * Returns the active plan tier (e.g. "BASIC", "PREMIUM") for the given user and category.
     * Returns null if no active subscription found.
     */
    public String getActivePlanTier(UUID userId, String category) {
        try {
            String url = paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/plan";
            if (category != null) {
                url += "?category=" + category;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("planType") instanceof String) {
                String planType = (String) response.get("planType");
                return "NONE".equalsIgnoreCase(planType) ? null : planType;
            }
            return null;
        } catch (RestClientException e) {
            log.warn("payment-service plan tier unavailable for {}/category={}: {}", userId, category, e.getMessage());
            return null;
        }
    }
}

