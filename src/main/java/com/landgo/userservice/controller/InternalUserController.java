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
import com.landgo.userservice.service.EmailService;
import com.landgo.userservice.dto.request.EmailRequest;
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
    private final EmailService emailService;

    /**
     * Single outbound mail path for every LandGo service.
     *
     * <p>Deduplication lives here rather than in each caller, so a Stripe webhook replay from
     * payment-service and a retried listing transition from core-service are both covered by one
     * implementation. Returns 202: the send is queued, and a delivery failure must never fail the
     * caller's business transaction.
     */
    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmail(@jakarta.validation.Valid @RequestBody EmailRequest request) {
        if (request.getHtmlBody() != null && !request.getHtmlBody().isBlank()) {
            emailService.sendTransactionalHtmlEmail(request.getToEmail(), request.getSubject(),
                    request.getHtmlBody(), request.getTemplateName(), request.getIdempotencyKey());
        } else {
            emailService.sendTransactionalTemplateEmail(request.getToEmail(), request.getSubject(),
                    request.getTemplateName(), request.getVariables(), request.getIdempotencyKey());
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable UUID userId, @RequestParam Role role) {
        authService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    /**
     * Legacy incremental grant. Superseded by {@link #setListingCredits}: payment-service's credit
     * ledger is the authority, and this endpoint could not express a correction or a reversal.
     *
     * @deprecated kept so an older payment-service build in flight does not 404
     */
    @Deprecated
    @PutMapping("/{userId}/add-listing-credits")
    public ResponseEntity<UserResponse> addListingCredits(@PathVariable UUID userId, @RequestParam(defaultValue = "1") int credits) {
        return ResponseEntity.ok(authService.addListingCredits(userId, credits));
    }

    /**
     * Mirrors the authoritative purchased-credit total from payment-service onto the user record.
     *
     * <p>A mirror, not a source: listing creation is gated on payment-service's ledger. This
     * exists so clients still reading {@code maxListings} see the real purchased total instead of
     * a stale plan-tier cap.
     */
    @PutMapping("/{userId}/listing-credits")
    public ResponseEntity<UserResponse> setListingCredits(
            @PathVariable UUID userId, @RequestParam int creditsPurchased) {
        return ResponseEntity.ok(authService.setListingCreditsPurchased(userId, creditsPurchased));
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