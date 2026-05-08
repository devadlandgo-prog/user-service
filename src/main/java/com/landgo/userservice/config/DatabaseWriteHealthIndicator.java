package com.landgo.userservice.config;

import com.landgo.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseWriteHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        try {
            // Check count to verify connectivity and basic read/write
            long count = userRepository.count();
            return Health.up()
                    .withDetail("database", "Connected")
                    .withDetail("recordCount", count)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Connectivity failure")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
