package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.UpdateProfileRequest;
import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.PageResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.service.AuthService;
import com.landgo.userservice.service.VendorService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

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

    @GetMapping("/vendors/batch")
    public ResponseEntity<java.util.Map<UUID, VendorResponse>> getVendorProfilesBatch(@RequestParam List<UUID> userIds) {
        return ResponseEntity.ok(vendorService.getVendorProfilesBatch(userIds));
    }

    @PostMapping("/{userId}/vendor")
    public ResponseEntity<VendorResponse> createVendorProfile(
            @PathVariable UUID userId,
            @RequestBody VendorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.createVendorProfile(userId, request));
    }

    @GetMapping("/{userId}/verification-code")
    public ResponseEntity<String> getVerificationCode(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getLatestVerificationCode(userId));
    }

    // ── Admin Professional Management ───────────────────────────────────────

    @GetMapping("/professionals")
    public ResponseEntity<PageResponse<UserResponse>> getAllProfessionals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> professionals = authService.getAllProfessionals(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        PageResponse<UserResponse> response = PageResponse.<UserResponse>builder()
                .content(professionals.getContent())
                .number(professionals.getNumber())
                .size(professionals.getSize())
                .totalElements(professionals.getTotalElements())
                .totalPages(professionals.getTotalPages())
                .first(professionals.isFirst())
                .last(professionals.isLast())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/professional")
    public ResponseEntity<UserResponse> updateProfessionalProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfessionalProfile(userId, request));
    }

    @DeleteMapping("/{userId}/professional")
    public ResponseEntity<Void> deactivateProfessional(@PathVariable UUID userId) {
        authService.deactivateProfessional(userId);
        return ResponseEntity.noContent().build();
    }
}