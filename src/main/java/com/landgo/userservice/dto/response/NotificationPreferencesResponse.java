package com.landgo.userservice.dto.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationPreferencesResponse {
    private UUID id;
    private boolean listingAlerts;
    private boolean priceDrops;
    private boolean marketingEmails;
    private boolean securityAlerts;
}
