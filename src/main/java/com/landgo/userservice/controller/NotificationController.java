package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User inbox alerts API")
public class NotificationController {

    private final com.landgo.userservice.service.NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user inbox alerts")
    public ResponseEntity<ApiResponse<List<com.landgo.userservice.entity.Notification>>> getNotifications(@CurrentUser UserPrincipal userPrincipal) {
        List<com.landgo.userservice.entity.Notification> alerts = notificationService.getNotifications(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark alert as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        notificationService.markAsRead(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", null));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Empty inbox")
    public ResponseEntity<ApiResponse<Void>> clearInbox(@CurrentUser UserPrincipal userPrincipal) {
        notificationService.clearInbox(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Inbox cleared", null));
    }
}
