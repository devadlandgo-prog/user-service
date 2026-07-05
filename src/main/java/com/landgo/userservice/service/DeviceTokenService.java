package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.DeviceTokenRequest;
import com.landgo.userservice.dto.response.DeviceTokenResponse;
import com.landgo.userservice.entity.UserDeviceToken;
import com.landgo.userservice.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Transactional
    public DeviceTokenResponse registerToken(UUID userId, DeviceTokenRequest request) {
        Optional<UserDeviceToken> existingTokenOpt = userDeviceTokenRepository.findByFcmToken(request.getFcmToken());

        UserDeviceToken token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            // If the token was previously registered to another user, update it
            if (!token.getUserId().equals(userId)) {
                log.info("Reassigning FCM token to new user {}", userId);
                token.setUserId(userId);
            }
            token.setPlatform(request.getPlatform());
            token.setDeviceLabel(request.getDeviceLabel());
            token.setAppVersion(request.getAppVersion());
            token.setActive(true);
            token.setLastSeenAt(LocalDateTime.now());
        } else {
            token = UserDeviceToken.builder()
                    .userId(userId)
                    .fcmToken(request.getFcmToken())
                    .platform(request.getPlatform())
                    .deviceLabel(request.getDeviceLabel())
                    .appVersion(request.getAppVersion())
                    .active(true)
                    .build();
        }

        token = userDeviceTokenRepository.save(token);
        
        return DeviceTokenResponse.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .platform(token.getPlatform())
                .active(token.isActive())
                .build();
    }

    @Transactional
    public void unregisterToken(UUID userId, DeviceTokenRequest request) {
        Optional<UserDeviceToken> existingTokenOpt = userDeviceTokenRepository.findByFcmToken(request.getFcmToken());
        if (existingTokenOpt.isPresent()) {
            UserDeviceToken token = existingTokenOpt.get();
            if (token.getUserId().equals(userId)) {
                token.setActive(false);
                userDeviceTokenRepository.save(token);
                log.info("Unregistered FCM token for user {}", userId);
            }
        }
    }
}
