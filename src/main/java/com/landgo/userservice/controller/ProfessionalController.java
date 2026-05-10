package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.ProfessionalRegisterRequest;
import com.landgo.userservice.dto.request.UpdateProfileRequest;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.PageResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.AuthService;
import com.landgo.userservice.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional profile, search, and admin management APIs")
public class ProfessionalController {

    private final VendorService vendorService;
    private final AuthService authService;

    // ── Public APIs ──────────────────────────────────────────────────────────

    @GetMapping("/expertise-options")
    @Operation(summary = "Get available expertise options")
    public ResponseEntity<ApiResponse<String[]>> getExpertiseOptions() {
        String[] options = { "Land Surveying", "Architecture", "Legal Advice", "Civil Engineering",
                "Environmental Assessment", "Urban Planning", "Real Estate Law", "Property Appraisal" };
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping
    @Operation(summary = "List verified professionals")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getVerifiedProfessionals(
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
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search professionals")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchProfessionals(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> professionals = authService.searchProfessionals(q,
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
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get professional profile by user ID")
    public ResponseEntity<ApiResponse<VendorResponse>> getProfessionalById(@PathVariable UUID id) {
        VendorResponse vendor = vendorService.getVendorProfile(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @GetMapping("/me/dashboard")
    @Operation(summary = "Get my professional dashboard")
    public ResponseEntity<ApiResponse<UserResponse>> getMyDashboard(@CurrentUser UserPrincipal userPrincipal) {
        UserResponse user = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/register")
    @Operation(summary = "Register as a professional (Unified account + profile)")
    public ResponseEntity<ApiResponse<VendorResponse>> registerProfessional(
            @Valid @RequestBody ProfessionalRegisterRequest request) {
        log.info("Unified professional registration request for email: {}", request.getEmail());
        VendorResponse response = vendorService.registerProfessional(request);
        return ResponseEntity.ok(ApiResponse.success("Professional registered successfully", response));
    }

    // ── Admin APIs ───────────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all professionals (admin)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllProfessionalsForAdmin(
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
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a professional's profile (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfessionalForAdmin(
            @PathVariable UUID id,
            @RequestBody UpdateProfileRequest request) {
        UserResponse updated = authService.updateProfessionalProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Professional profile updated", updated));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a professional (admin)")
    public ResponseEntity<ApiResponse<Void>> deactivateProfessionalForAdmin(@PathVariable UUID id) {
        authService.deactivateProfessional(id);
        return ResponseEntity.ok(ApiResponse.success("Professional deactivated successfully", null));
    }
}
