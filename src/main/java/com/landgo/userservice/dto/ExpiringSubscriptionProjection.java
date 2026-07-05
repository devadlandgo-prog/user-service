package com.landgo.userservice.dto;

import java.time.LocalDateTime;

public interface ExpiringSubscriptionProjection {
    String getEmail();
    String getFullName();
    LocalDateTime getEndDate();
    String getPlanCategory();
}
