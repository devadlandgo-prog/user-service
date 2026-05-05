package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.NotificationPreferencesRequest;
import com.landgo.userservice.dto.request.UpdateProfileRequest;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.NotificationPreferencesResponse;
import com.landgo.userservice.dto.response.PageResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.dto.response.UserStatsResponse;
import com.landgo.userservice.service.AuthService;
import com.landgo.userservice.service.NotificationPreferencesService;
import com.landgo.userservice.service.UserStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "User Profile & Settings", description = "User profile, password, and notification preference APIs")
public class UserController {

    private final AuthService authService;
    private final NotificationPreferencesService notificationPreferencesService;
    private final UserStatsService userStatsService;

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@CurrentUser UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update profile (partial)")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile (full)")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfilePut(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@CurrentUser UserPrincipal userPrincipal) {
        authService.deleteAccount(userPrincipal);
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
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @CurrentUser UserPrincipal userPrincipal) {
        UserStatsResponse stats = userStatsService.buildStats(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PatchMapping("/users/me/mfa")
    @Operation(summary = "Toggle MFA status")
    public ResponseEntity<ApiResponse<UserResponse>> updateMfaStatus(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody com.landgo.userservice.dto.request.MfaSetupRequest request) {
        UserResponse response = authService.updateMfaStatus(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("MFA status updated", response));
    }

    // ── Admin APIs ──────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "List all users (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<UserResponse> users =
                authService.getAllUsers(pageable);
        PageResponse<UserResponse> response = PageResponse.<UserResponse>builder()
                .content(users.getContent())
                .number(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
