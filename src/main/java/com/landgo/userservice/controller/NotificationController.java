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

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User inbox alerts API")
public class NotificationController {

    @GetMapping
    @Operation(summary = "Get user inbox alerts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotifications(@CurrentUser UserPrincipal userPrincipal) {
        // Stub for now
        List<Map<String, Object>> alerts = List.of(
                Map.of("id", "1", "message", "Welcome to LandGo!", "isRead", false, "createdAt", LocalDateTime.now())
        );
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark alert as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable String id) {
        // Stub for now
        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", null));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Empty inbox")
    public ResponseEntity<ApiResponse<Void>> clearInbox(@CurrentUser UserPrincipal userPrincipal) {
        // Stub for now
        return ResponseEntity.ok(ApiResponse.success("Inbox cleared", null));
    }
}
