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
import com.landgo.userservice.exception.BadRequestException;

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
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                name -> name.toLowerCase(),
                                name -> name,
                                (existing, replacement) -> existing,
                                java.util.LinkedHashMap::new),
                        map -> {
                            java.util.List<String> result = new java.util.ArrayList<>(map.values());
                            if (result.stream().noneMatch(name -> "CONTRACTOR".equalsIgnoreCase(name))) {
                                result.add("CONTRACTOR");
                            }
                            return result;
                        }));
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

    @DeleteMapping("/expertise-options/{identifier}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete an expertise option by ID or name")
    public ResponseEntity<ApiResponse<Void>> deleteExpertise(@PathVariable String identifier) {
        // Try to parse as UUID first, if fails, treat as name
        try {
            UUID id = UUID.fromString(identifier);
            expertiseService.deleteExpertise(id);
        } catch (IllegalArgumentException e) {
            // Not a UUID, treat as name
            expertiseService.deleteExpertiseByName(identifier);
        }
        return ResponseEntity.ok(ApiResponse.success("Expertise deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "List verified professionals")
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> getVerifiedProfessionals(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Sort sort = Sort.by(Sort.Order.desc("createdAt").nullsLast());
        if (sortBy != null) {
            sort = switch (sortBy.toLowerCase()) {
                case "rating" -> Sort.by(Sort.Order.desc("rating").nullsLast());
                case "most_reviews" -> Sort.by(Sort.Order.desc("totalReviews").nullsLast());
                case "most_experience" -> Sort.by(Sort.Order.desc("yearsOfExperience").nullsLast());
                case "newest" -> Sort.by(Sort.Order.desc("createdAt").nullsLast());
                default -> sort;
            };
        }

        Page<VendorResponse> professionals = vendorService.getVerifiedProfessionals(
                specialization, PageRequest.of(page, size, sort));
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
        
        Sort sort = Sort.by(Sort.Order.desc("createdAt").nullsLast());
        if (sortBy != null) {
            sort = switch (sortBy.toLowerCase()) {
                case "rating" -> Sort.by(Sort.Order.desc("rating").nullsLast());
                case "most_reviews" -> Sort.by(Sort.Order.desc("totalReviews").nullsLast());
                case "most_experience" -> Sort.by(Sort.Order.desc("yearsOfExperience").nullsLast());
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
    @Operation(summary = "Get professional profile by vendor id or userId")
    public ResponseEntity<ApiResponse<VendorResponse>> getProfessionalById(@PathVariable String id) {
        UUID identifier;
        try {
            identifier = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid professional id format", "VALIDATION_ERROR");
        }
        VendorResponse vendor = vendorService.getVendorProfileResolved(identifier);
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

    @PostMapping("/{id}/view")
    @Operation(summary = "Increment profile view count")
    public ResponseEntity<ApiResponse<Void>> incrementProfileView(@PathVariable UUID id) {
        vendorService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success("Profile view count incremented", null));
    }
}
