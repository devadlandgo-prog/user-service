package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.DeviceTokenRequest;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.DeviceTokenResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FCM device token registration.
 *
 * <p>Two route shapes reach the same implementation: {@code /devices/tokens}, which LandGo Web
 * calls, and the original {@code /devices/me/token} used by the mobile clients.
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Device Tokens", description = "Register and unregister FCM device tokens for push delivery")
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/tokens")
    @Operation(summary = "Register an FCM device token",
            description = "Registers or refreshes the caller's FCM token. Idempotent — re-posting a known token reactivates it.")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> registerDeviceToken(
            @CurrentUser UserPrincipal userDetails,
            @Valid @RequestBody DeviceTokenRequest request) {

        DeviceTokenResponse response = deviceTokenService.registerToken(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/tokens")
    @Operation(summary = "Unregister an FCM device token",
            description = "Deactivates the supplied token. Pass the token as a query param: ?fcmToken=<token>. Called by LandGo Web on sign-out.")
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @CurrentUser UserPrincipal userDetails,
            @RequestParam String fcmToken) {

        if (fcmToken == null || fcmToken.isBlank()) {
            throw new com.landgo.userservice.exception.BadRequestException("fcmToken is required", "VALIDATION_ERROR");
        }
        deviceTokenService.unregisterToken(userDetails.getId(), fcmToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me/token")
    @Operation(summary = "Register an FCM device token (legacy path)",
            description = "Equivalent to POST /devices/tokens; retained for existing mobile clients.")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> registerToken(
            @CurrentUser UserPrincipal userDetails,
            @Valid @RequestBody DeviceTokenRequest request) {

        DeviceTokenResponse response = deviceTokenService.registerToken(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me/token")
    @Operation(summary = "Unregister an FCM device token (legacy path)",
            description = "Equivalent to DELETE /devices/tokens; retained for existing mobile clients. Pass token as ?fcmToken=<token>.")
    public ResponseEntity<ApiResponse<Void>> unregisterToken(
            @CurrentUser UserPrincipal userDetails,
            @RequestParam(required = false) String fcmToken) {

        if (fcmToken != null && !fcmToken.isBlank()) {
            deviceTokenService.unregisterToken(userDetails.getId(), fcmToken);
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
