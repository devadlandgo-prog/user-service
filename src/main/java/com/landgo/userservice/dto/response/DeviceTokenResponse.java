package com.landgo.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DeviceTokenResponse {
    private UUID id;
    private UUID userId;
    private String fcmToken;
    private String platform;
    /** Same value as {@code platform}, under the name LandGo Web expects. */
    private String deviceType;
    private boolean active;
}
