package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body for token unregistration.
 *
 * <p>Separate from {@link DeviceTokenRequest} because sign-out only knows the token — requiring
 * {@code platform} here would reject the payload LandGo Web sends on logout.
 */
@Data
public class DeviceTokenUnregisterRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}
