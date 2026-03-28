package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.NotificationPreferencesRequest;
import com.landgo.userservice.dto.response.NotificationPreferencesResponse;
import com.landgo.userservice.entity.NotificationPreferences;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.repository.NotificationPreferencesRepository;
import com.landgo.userservice.repository.UserRepository;
import com.landgo.userservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferencesService {

    private final NotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(UserPrincipal userPrincipal) {
        NotificationPreferences prefs = preferencesRepository.findByUserId(userPrincipal.getId())
                .orElseGet(() -> {
                    User user = userRepository.findById(userPrincipal.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return NotificationPreferences.builder()
                            .user(user).listingAlerts(true).priceDrops(true)
                            .marketingEmails(false).securityAlerts(true).build();
                });
        return toResponse(prefs);
    }

    @Transactional
    public NotificationPreferencesResponse updatePreferences(UserPrincipal userPrincipal, NotificationPreferencesRequest request) {
        NotificationPreferences prefs = preferencesRepository.findByUserId(userPrincipal.getId())
                .orElseGet(() -> {
                    User user = userRepository.findById(userPrincipal.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return NotificationPreferences.builder().user(user).build();
                });

        if (request.getListingAlerts() != null) prefs.setListingAlerts(request.getListingAlerts());
        if (request.getPriceDrops() != null) prefs.setPriceDrops(request.getPriceDrops());
        if (request.getMarketingEmails() != null) prefs.setMarketingEmails(request.getMarketingEmails());
        if (request.getSecurityAlerts() != null) prefs.setSecurityAlerts(request.getSecurityAlerts());

        prefs = preferencesRepository.save(prefs);
        log.info("Notification preferences updated for user: {}", userPrincipal.getId());
        return toResponse(prefs);
    }

    private NotificationPreferencesResponse toResponse(NotificationPreferences prefs) {
        return NotificationPreferencesResponse.builder()
                .listingAlerts(prefs.isListingAlerts())
                .priceDrops(prefs.isPriceDrops())
                .marketingEmails(prefs.isMarketingEmails())
                .securityAlerts(prefs.isSecurityAlerts())
                .build();
    }
}
