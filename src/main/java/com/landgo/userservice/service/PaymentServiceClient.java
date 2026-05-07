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

    /**
     * @return {@code 1} if the user has at least one active subscription, else {@code 0}.
     */
    public long countActiveSubscriptions(UUID userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    paymentServiceUrl + "/internal/subscriptions/user/" + userId + "/active",
                    Map.class);
            if (response == null || !Boolean.TRUE.equals(response.get("active"))) {
                return 0;
            }
            return 1;
        } catch (RestClientException e) {
            log.warn("payment-service subscription check unavailable for {}: {}", userId, e.getMessage());
            return 0;
        }
    }
}
