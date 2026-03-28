package com.landgo.userservice.dto.request;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationPreferencesRequest {
    private Boolean listingAlerts;
    private Boolean priceDrops;
    private Boolean marketingEmails;
    private Boolean securityAlerts;
}
