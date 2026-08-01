package com.landgo.userservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    /**
     * WEB | ANDROID | IOS.
     *
     * <p>LandGo Web posts this field as {@code deviceType}; mobile clients post {@code platform}.
     * Both spellings are accepted so neither client has to change.
     */
    @NotBlank(message = "Platform is required")
    @JsonAlias("deviceType")
    private String platform;

    /** Device description; LandGo Web sends the browser user agent as {@code deviceModel}. */
    @JsonAlias("deviceModel")
    private String deviceLabel;

    private String appVersion;
}
