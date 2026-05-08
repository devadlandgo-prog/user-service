package com.landgo.userservice.service;

import com.landgo.userservice.dto.client.CoreVendorListingStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoreServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.core-service-url:http://localhost:8083}")
    private String coreServiceUrl;

    public CoreVendorListingStatsDto fetchVendorListingStats(UUID vendorUserId) {
        try {
            CoreVendorListingStatsDto body = restTemplate.getForObject(
                    coreServiceUrl + "/internal/vendors/" + vendorUserId + "/listing-stats",
                    CoreVendorListingStatsDto.class);
            return body != null ? body : emptyStats();
        } catch (RestClientException e) {
            log.warn("core-service listing stats unavailable for {}: {}", vendorUserId, e.getMessage());
            return emptyStats();
        }
    }

    private static CoreVendorListingStatsDto emptyStats() {
        return new CoreVendorListingStatsDto(0, 0, 0, 0);
    }
}
