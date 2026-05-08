package com.landgo.userservice.service;

import com.landgo.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persists login timestamps in a new transaction so callers can stay read-only.
 */
@Service
@RequiredArgsConstructor
public class LoginAuditService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLastLogin(UUID userId) {
        userRepository.updateLastLoginAt(userId, LocalDateTime.now());
    }
}
