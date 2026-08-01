package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.request.PushCampaignRequest;
import com.landgo.userservice.dto.request.PushTemplateRequest;
import com.landgo.userservice.dto.response.PushCampaignDetailResponse;
import com.landgo.userservice.dto.response.PushCampaignResponse;
import com.landgo.userservice.dto.response.PushTemplateResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.PushCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/push")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Push Notifications Admin", description = "Manage push notification templates and broadcast campaigns (admin only)")
public class NotificationAdminController {

    private final PushCampaignService pushCampaignService;

    // --- Templates ---

    @GetMapping("/templates")
    @Operation(summary = "List push notification templates", description = "Returns all saved push templates available for reuse when creating campaigns.")
    public ResponseEntity<ApiResponse<List<PushTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(pushCampaignService.getTemplates()));
    }

    @PostMapping("/templates")
    @Operation(summary = "Create a push notification template", description = "Saves a reusable title/body template. Returns 201 with the created template.")
    public ResponseEntity<ApiResponse<PushTemplateResponse>> createTemplate(
            @CurrentUser UserPrincipal adminDetails,
            @Valid @RequestBody PushTemplateRequest request) {
        PushTemplateResponse response = pushCampaignService.createTemplate(adminDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "Update a push notification template", description = "Replaces the title and/or body of an existing template.")
    public ResponseEntity<ApiResponse<PushTemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody PushTemplateRequest request) {
        PushTemplateResponse response = pushCampaignService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "Delete a push notification template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        pushCampaignService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- Campaigns ---

    @GetMapping("/campaigns")
    @Operation(summary = "List push campaigns (paginated)", description = "Returns all campaigns ordered by creation date descending. Supports standard Spring Pageable query params (page, size, sort).")
    public ResponseEntity<ApiResponse<Page<PushCampaignResponse>>> getCampaigns(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(pushCampaignService.getCampaigns(pageable)));
    }

    @GetMapping("/campaigns/{id}")
    @Operation(summary = "Get push campaign details", description = "Returns full campaign details including per-device delivery results.")
    public ResponseEntity<ApiResponse<PushCampaignDetailResponse>> getCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(pushCampaignService.getCampaign(id)));
    }

    @PostMapping("/campaigns")
    @Operation(summary = "Create and queue a push campaign", description = "Creates a new broadcast campaign. The campaign is queued immediately and delivered asynchronously by the background worker. Poll GET /campaigns/{id} for terminal status (DELIVERED or FAILED).")
    public ResponseEntity<ApiResponse<PushCampaignResponse>> createCampaign(
            @CurrentUser UserPrincipal adminDetails,
            @Valid @RequestBody PushCampaignRequest request) {
        PushCampaignResponse response = pushCampaignService.createCampaign(adminDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
