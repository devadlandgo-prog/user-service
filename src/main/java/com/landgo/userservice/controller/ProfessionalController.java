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
import java.util.List;

import com.landgo.userservice.service.ExpertiseService;
import com.landgo.userservice.dto.request.ExpertiseRequest;
import com.landgo.userservice.entity.Expertise;

@Slf4j
@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional profile, search, and admin management APIs")
public class ProfessionalController {

    private final VendorService vendorService;
    private final AuthService authService;
    private final ExpertiseService expertiseService;

    // ── Public APIs ──────────────────────────────────────────────────────────

    @GetMapping("/expertise-options")
    @Operation(summary = "Get available expertise options")
    public ResponseEntity<ApiResponse<List<String>>> getExpertiseOptions() {
        List<String> options = expertiseService.getAllExpertise(true).stream()
                .map(Expertise::getName)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @PostMapping("/expertise-options")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Create a new expertise option")
    public ResponseEntity<ApiResponse<Expertise>> createExpertise(
            @Valid @RequestBody ExpertiseRequest request) {
        var expertise = expertiseService.createExpertise(request.getName(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Expertise created successfully", expertise));
    }

    @PutMapping("/expertise-options/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update an expertise option")
    public ResponseEntity<ApiResponse<Expertise>> updateExpertise(
            @PathVariable UUID id,
            @Valid @RequestBody ExpertiseRequest request) {
        var expertise = expertiseService.updateExpertise(id, request.getName(), request.getDescription(), request.getActive());
        return ResponseEntity.ok(ApiResponse.success("Expertise updated successfully", expertise));
    }

    @DeleteMapping("/expertise-options/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete an expertise option")
    public ResponseEntity<ApiResponse<Void>> deleteExpertise(@PathVariable UUID id) {
        expertiseService.deleteExpertise(id);
        return ResponseEntity.ok(ApiResponse.success("Expertise deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "List verified professionals")
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> getVerifiedProfessionals(
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null) {
            sort = switch (sortBy.toLowerCase()) {
                case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
                case "most_reviews" -> Sort.by(Sort.Direction.DESC, "totalReviews");
                case "most_experience" -> Sort.by(Sort.Direction.DESC, "yearsOfExperience");
                default -> sort;
            };
        }

        Page<VendorResponse> professionals = vendorService.getVerifiedProfessionals(
                PageRequest.of(page, size, sort));
        PageResponse<VendorResponse> response = PageResponse.<VendorResponse>builder()
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
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> searchProfessionals(
            @RequestParam String q,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null) {
            sort = switch (sortBy.toLowerCase()) {
                case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
                case "most_reviews" -> Sort.by(Sort.Direction.DESC, "totalReviews");
                case "most_experience" -> Sort.by(Sort.Direction.DESC, "yearsOfExperience");
                default -> sort;
            };
        }

        Page<VendorResponse> professionals = vendorService.searchProfessionals(q,
                PageRequest.of(page, size, sort));
        PageResponse<VendorResponse> response = PageResponse.<VendorResponse>builder()
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
    @Operation(summary = "Register as a professional (public unified signup or authenticated profile create)")
    public ResponseEntity<ApiResponse<VendorResponse>> registerProfessional(
            @Valid @RequestBody ProfessionalRegisterRequest request,
            @CurrentUser UserPrincipal userPrincipal) {
        log.info("Unified professional registration request for email: {}", request.getEmail());
        VendorResponse response = vendorService.registerProfessional(request, userPrincipal);
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

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a professional's profile details (admin)")
    public ResponseEntity<ApiResponse<VendorResponse>> getProfessionalByIdForAdmin(@PathVariable UUID id) {
        VendorResponse vendor = vendorService.getVendorProfile(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
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
    
    @PatchMapping("/admin/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify/Approve a professional (admin)")
    public ResponseEntity<ApiResponse<VendorResponse>> verifyProfessional(
            @PathVariable UUID id,
            @RequestParam com.landgo.userservice.enums.VerificationStatus status,
            @RequestParam(required = false) String notes) {
        VendorResponse response = vendorService.verifyProfessional(id, status, notes);
        return ResponseEntity.ok(ApiResponse.success("Professional verification updated", response));
    }
}
