package com.landgo.userservice.service;

import com.landgo.userservice.entity.UserDeviceToken;
import com.landgo.userservice.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Maps an admin-selected audience onto the active device tokens it targets.
 *
 * <p>Accepts both the segment names used by the admin portal ({@code ALL_USERS}, {@code WEB_ONLY},
 * {@code MOBILE_ONLY}) and the shorter forms already stored on historical campaigns
 * ({@code ALL}, {@code BUYERS}, …), so existing rows keep resolving after this change.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushAudienceResolver {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Transactional(readOnly = true)
    public List<UserDeviceToken> resolve(String audience, Map<String, Object> filter) {
        String normalized = audience == null ? "" : audience.trim().toUpperCase();

        return switch (normalized) {
            case "ALL", "ALL_USERS" -> userDeviceTokenRepository.findAllActiveDevices();
            case "WEB", "WEB_ONLY" -> userDeviceTokenRepository.findActiveDevicesByPlatforms(List.of("WEB"));
            case "MOBILE", "MOBILE_ONLY" ->
                    userDeviceTokenRepository.findActiveDevicesByPlatforms(List.of("ANDROID", "IOS"));
            case "BUYERS" -> userDeviceTokenRepository.findActiveDevicesForBuyers();
            case "SELLERS" -> userDeviceTokenRepository.findActiveDevicesForSellers();
            case "VENDORS" -> userDeviceTokenRepository.findActiveDevicesForVendors();
            case "SINGLE_USER" -> resolveSingleUser(filter);
            case "TEST" -> resolveTestToken(filter);
            default -> {
                log.warn("Unknown push audience '{}'; resolving to no recipients", audience);
                yield List.of();
            }
        };
    }

    private List<UserDeviceToken> resolveSingleUser(Map<String, Object> filter) {
        Object userId = filter == null ? null : filter.get("userId");
        if (userId == null) {
            log.warn("SINGLE_USER audience is missing a 'userId' filter");
            return List.of();
        }

        try {
            return userDeviceTokenRepository.findActiveDevicesForUser(UUID.fromString(userId.toString()));
        } catch (IllegalArgumentException e) {
            log.warn("SINGLE_USER audience has a malformed 'userId' filter: {}", userId);
            return List.of();
        }
    }

    private List<UserDeviceToken> resolveTestToken(Map<String, Object> filter) {
        Object token = filter == null ? null : filter.get("fcmToken");
        if (token == null) {
            log.warn("TEST audience is missing an 'fcmToken' filter");
            return List.of();
        }

        return userDeviceTokenRepository.findByFcmTokenAndActiveTrue(token.toString())
                .map(List::of)
                .orElseGet(() -> {
                    log.warn("TEST audience token is not registered or is inactive");
                    return List.of();
                });
    }
}
