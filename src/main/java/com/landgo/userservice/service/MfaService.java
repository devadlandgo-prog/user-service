package com.landgo.userservice.service;

import com.landgo.userservice.entity.User;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.mfa.strategy.MfaStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private final List<MfaStrategy> strategies;

    public void initiateMfa(User user) {
        if (!user.isMfaEnabled()) {
            log.debug("MFA not enabled for user: {}", user.getId());
            return;
        }
        
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            log.warn("MFA initiation failed for user {}: Phone number missing", user.getId());
            throw new BadRequestException("Phone number is required to receive MFA codes. Please update your profile.", "MFA_PHONE_REQUIRED");
        }

        // Default to SMS strategy for now. In a full SOLID implementation, 
        // we'd pick the user's preferred strategy from the list.
        MfaStrategy strategy = strategies.stream()
                .filter(s -> s.getMethodName().equalsIgnoreCase("SMS"))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("MFA Strategy 'SMS' not found in registry");
                    return new RuntimeException("MFA Strategy not found");
                });

        log.info("Initiating MFA via {} for user: {}", strategy.getMethodName(), user.getId());
        strategy.sendCode(user);
    }

    public boolean verifyMfa(User user, String code) {
        MfaStrategy strategy = strategies.stream()
                .filter(s -> s.getMethodName().equalsIgnoreCase("SMS"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("MFA Strategy not found"));

        return strategy.verifyCode(user, code);
    }
}
