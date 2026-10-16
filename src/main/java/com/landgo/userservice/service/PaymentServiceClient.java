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
            if (response == null) {
                return null;
            }
            // payment-service wraps this one in a "data" envelope. Reading only the top level meant
            // every lookup returned null, so a professional's plan tier never resolved.
            Object body = response.get("data") instanceof Map<?, ?> nested ? nested.get("planType") : response.get("planType");
            if (body instanceof String planType) {
                return "NONE".equalsIgnoreCase(planType) ? null : planType;
            }
            return null;
        } catch (RestClientException e) {
            log.warn("payment-service plan tier unavailable for {}/category={}: {}", userId, category, e.getMessage());
            return null;
        }
    }

    /**
     * Expertise allowance for the user's active market profession subscription.
     *
     * <p>Returns {@link ExpertiseEntitlement#unavailable()} when payment-service cannot be reached.
     * That is deliberately not the same as an allowance of zero: refusing a registration because a
     * sibling service is down would be the wrong failure, and letting it through unchecked would be
     * worse, so the caller decides.
     */
    public ExpertiseEntitlement getExpertiseEntitlement(UUID userId) {
        try {
            String url = paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/expertise-limit";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return ExpertiseEntitlement.unavailable();
            }
            boolean active = Boolean.TRUE.equals(response.get("active"));
            boolean unlimited = Boolean.TRUE.equals(response.get("unlimited"));
            Integer limit = response.get("maxExpertiseSelections") instanceof Number n ? n.intValue() : null;
            return new ExpertiseEntitlement(true, active, unlimited, limit);
        } catch (RestClientException e) {
            log.warn("payment-service expertise entitlement unavailable for {}: {}", userId, e.getMessage());
            return ExpertiseEntitlement.unavailable();
        }
    }

    /**
     * @param known     false when payment-service could not be reached, so nothing here is a fact
     * @param active    whether a live paid market profession subscription backs the allowance
     * @param unlimited whether the plan grants unlimited selections
     * @param limit     the cap when it is a number; null when unlimited or unknown
     */
    public record ExpertiseEntitlement(boolean known, boolean active, boolean unlimited, Integer limit) {
        public static ExpertiseEntitlement unavailable() {
            return new ExpertiseEntitlement(false, false, false, null);
        }
    }
}

