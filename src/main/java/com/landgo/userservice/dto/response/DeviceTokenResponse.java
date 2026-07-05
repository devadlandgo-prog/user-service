package com.landgo.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DeviceTokenResponse {
    private UUID id;
    private UUID userId;
    private String platform;
    private boolean active;
}
