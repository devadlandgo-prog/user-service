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
            return;
        }
        
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new BadRequestException("Phone number is required for MFA", "MFA_PHONE_REQUIRED");
        }

        // Default to SMS strategy for now. In a full SOLID implementation, 
        // we'd pick the user's preferred strategy from the list.
        MfaStrategy strategy = strategies.stream()
                .filter(s -> s.getMethodName().equalsIgnoreCase("SMS"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("MFA Strategy not found"));

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
