package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.request.PushCampaignRequest;
import com.landgo.userservice.dto.request.PushTemplateRequest;
import com.landgo.userservice.dto.response.PushCampaignResponse;
import com.landgo.userservice.dto.response.PushTemplateResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.PushCampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/push")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationAdminController {

    private final PushCampaignService pushCampaignService;

    // --- Templates ---

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<PushTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(pushCampaignService.getTemplates()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<PushTemplateResponse>> createTemplate(
            @CurrentUser UserPrincipal adminDetails,
            @Valid @RequestBody PushTemplateRequest request) {
        PushTemplateResponse response = pushCampaignService.createTemplate(adminDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<PushTemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody PushTemplateRequest request) {
        PushTemplateResponse response = pushCampaignService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        pushCampaignService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- Campaigns ---

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<Page<PushCampaignResponse>>> getCampaigns(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(pushCampaignService.getCampaigns(pageable)));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<PushCampaignResponse>> createCampaign(
            @CurrentUser UserPrincipal adminDetails,
            @Valid @RequestBody PushCampaignRequest request) {
        PushCampaignResponse response = pushCampaignService.createCampaign(adminDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
