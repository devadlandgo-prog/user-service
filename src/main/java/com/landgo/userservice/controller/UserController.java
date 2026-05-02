package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.NotificationPreferencesRequest;
import com.landgo.userservice.dto.request.UpdateProfileRequest;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.NotificationPreferencesResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.AuthService;
import com.landgo.userservice.service.NotificationPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "User Profile & Settings", description = "User profile, password, and notification preference APIs")
public class UserController {

    private final AuthService authService;
    private final NotificationPreferencesService notificationPreferencesService;

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@CurrentUser UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@CurrentUser UserPrincipal userPrincipal) {
        // Stub for account deletion logic
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @GetMapping("/users/me/notification-settings")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getNotificationPreferences(
            @CurrentUser UserPrincipal userPrincipal) {
        NotificationPreferencesResponse response = notificationPreferencesService.getPreferences(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/me/notification-settings")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updateNotificationPreferences(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody NotificationPreferencesRequest request) {
        NotificationPreferencesResponse response = notificationPreferencesService.updatePreferences(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", response));
    }

    @GetMapping("/users/me/stats")
    @Operation(summary = "Get user aggregate metrics")
    public ResponseEntity<ApiResponse<com.landgo.userservice.dto.response.UserStatsResponse>> getUserStats(
            @CurrentUser UserPrincipal userPrincipal) {
        // Stub for now
        com.landgo.userservice.dto.response.UserStatsResponse stats = com.landgo.userservice.dto.response.UserStatsResponse.builder()
                .activeListings(5)
                .totalViews(1250)
                .build();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
