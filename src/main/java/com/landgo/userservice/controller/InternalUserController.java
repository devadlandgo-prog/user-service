package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.service.AuthService;
import com.landgo.userservice.service.VendorService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal API — called by payment-service and core-service via REST.
 * Not exposed to external clients (secured by network policy in production).
 */
@Hidden
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;
    private final VendorService vendorService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable UUID userId, @RequestParam Role role) {
        authService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/vendor")
    public ResponseEntity<VendorResponse> getVendorProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(vendorService.getVendorProfile(userId));
    }
}
