package com.landgo.userservice.service;

import com.landgo.userservice.dto.ExpiringSubscriptionProjection;
import com.landgo.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionExpiryWorker {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Run every day at 9:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAndNotifyExpiringSubscriptions() {
        log.info("Starting subscription expiration check...");

        // 1. Check for 7 days left (1 week)
        try {
            List<ExpiringSubscriptionProjection> expiringInSevenDays = userRepository.findUsersWithExpiringSubscriptions(7);
            log.info("Found {} subscriptions expiring in 7 days.", expiringInSevenDays.size());
            for (ExpiringSubscriptionProjection projection : expiringInSevenDays) {
                emailService.sendSubscriptionExpiryEmail(
                        projection.getEmail(),
                        projection.getFullName(),
                        7,
                        projection.getPlanCategory()
                );
            }
        } catch (Exception e) {
            log.error("Error checking subscriptions expiring in 7 days", e);
        }

        // 2. Check for 1 day left
        try {
            List<ExpiringSubscriptionProjection> expiringInOneDay = userRepository.findUsersWithExpiringSubscriptions(1);
            log.info("Found {} subscriptions expiring in 1 day.", expiringInOneDay.size());
            for (ExpiringSubscriptionProjection projection : expiringInOneDay) {
                emailService.sendSubscriptionExpiryEmail(
                        projection.getEmail(),
                        projection.getFullName(),
                        1,
                        projection.getPlanCategory()
                );
            }
        } catch (Exception e) {
            log.error("Error checking subscriptions expiring in 1 day", e);
        }

        log.info("Subscription expiration check complete.");
    }
}
