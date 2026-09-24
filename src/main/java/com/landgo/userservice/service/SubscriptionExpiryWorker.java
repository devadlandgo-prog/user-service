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
    /**
     * Warns subscribers whose paid-through date is approaching.
     *
     * <p>Land listing credits are excluded by the query itself: they are one-time purchases that
     * never expire, so a countdown for them would be wrong. Each warning is deduplicated per
     * recipient, plan and threshold, so a re-run on the same day sends nothing.
     */
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

        // 2. Check for 3 days left. Three rather than one because the supplied
        // SubscriptionExpiring template is written as the three-day notice, and a
        // one-day warning leaves a subscriber almost no time to act on it.
        try {
            List<ExpiringSubscriptionProjection> expiringInThreeDays = userRepository.findUsersWithExpiringSubscriptions(3);
            log.info("Found {} subscriptions expiring in 3 days.", expiringInThreeDays.size());
            for (ExpiringSubscriptionProjection projection : expiringInThreeDays) {
                emailService.sendSubscriptionExpiryEmail(
                        projection.getEmail(),
                        projection.getFullName(),
                        3,
                        projection.getPlanCategory()
                );
            }
        } catch (Exception e) {
            log.error("Error checking subscriptions expiring in 3 days", e);
        }

        log.info("Subscription expiration check complete.");
    }
}
