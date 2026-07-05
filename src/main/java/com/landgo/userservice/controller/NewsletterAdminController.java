package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.request.NewsletterPublishRequest;
import com.landgo.userservice.entity.NewsletterCampaign;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/newsletter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NewsletterAdminController {

    private final NewsletterService newsletterService;

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<NewsletterCampaign>> publishNewsletter(
            @CurrentUser UserPrincipal adminDetails,
            @Valid @RequestBody NewsletterPublishRequest request) {
        
        NewsletterCampaign campaign = newsletterService.initiatePublish(adminDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(campaign));
    }
}
