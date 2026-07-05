package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {
    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    @NotBlank(message = "Platform is required")
    private String platform; // WEB | ANDROID | IOS

    private String deviceLabel;
    
    private String appVersion;
}
