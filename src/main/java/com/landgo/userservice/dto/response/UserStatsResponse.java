package com.landgo.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private long activeListings;
    private long totalListings;
    private long totalViews;
    private long totalEnquiries;
    private long activeSubscriptions;
    private int profileCompleteness;
    private java.time.LocalDateTime lastLoginAt;
    private long memberSinceMonths;
}
