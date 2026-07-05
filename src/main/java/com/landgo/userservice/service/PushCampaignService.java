package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.PushCampaignRequest;
import com.landgo.userservice.dto.request.PushTemplateRequest;
import com.landgo.userservice.dto.response.PushCampaignResponse;
import com.landgo.userservice.dto.response.PushTemplateResponse;
import com.landgo.userservice.entity.PushCampaign;
import com.landgo.userservice.entity.PushTemplate;
import com.landgo.userservice.repository.PushCampaignRepository;
import com.landgo.userservice.repository.PushTemplateRepository;
import com.landgo.userservice.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushCampaignService {

    private final PushTemplateRepository pushTemplateRepository;
    private final PushCampaignRepository pushCampaignRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public List<PushTemplateResponse> getTemplates() {
        return pushTemplateRepository.findAll().stream()
                .map(this::mapToTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PushTemplateResponse createTemplate(UUID adminUserId, PushTemplateRequest request) {
        PushTemplate template = PushTemplate.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .deepLink(request.getDeepLink())
                .createdBy(adminUserId)
                .build();
        return mapToTemplateResponse(pushTemplateRepository.save(template));
    }

    @Transactional
    public PushTemplateResponse updateTemplate(UUID id, PushTemplateRequest request) {
        PushTemplate template = pushTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        template.setTitle(request.getTitle());
        template.setBody(request.getBody());
        template.setImageUrl(request.getImageUrl());
        template.setDeepLink(request.getDeepLink());
        
        return mapToTemplateResponse(pushTemplateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        pushTemplateRepository.deleteById(id);
    }

    public Page<PushCampaignResponse> getCampaigns(Pageable pageable) {
        return pushCampaignRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToCampaignResponse);
    }

    @Transactional
    public PushCampaignResponse createCampaign(UUID adminUserId, PushCampaignRequest request) {
        // Resolve initial audience count just for estimation if needed, though actual send resolves again
        int targetedCount = 0; // This would typically be an estimate, but we'll leave it 0 until sending or we can resolve it now.
        
        // Count active tokens based on audience
        List<String> tokens = resolveTokensForAudience(request.getAudience(), request.getAudienceFilter());
        targetedCount = tokens.size();
        
        String status = request.isSendNow() ? "QUEUED" : "DRAFT";
        if (request.getScheduledAt() != null && request.getScheduledAt().isAfter(LocalDateTime.now())) {
            status = "QUEUED"; // It will be picked up when time arrives
        }

        PushCampaign campaign = PushCampaign.builder()
                .templateId(request.getTemplateId())
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .deepLink(request.getDeepLink())
                .audience(request.getAudience())
                .audienceFilter(request.getAudienceFilter())
                .status(status)
                .targetedCount(targetedCount)
                .sentBy(adminUserId)
                .scheduledAt(request.getScheduledAt())
                .build();

        return mapToCampaignResponse(pushCampaignRepository.save(campaign));
    }
    
    public List<String> resolveTokensForAudience(String audience, java.util.Map<String, Object> filter) {
        return switch (audience) {
            case "ALL" -> userDeviceTokenRepository.findAllActiveTokens();
            case "BUYERS" -> userDeviceTokenRepository.findActiveTokensForBuyers();
            case "SELLERS" -> userDeviceTokenRepository.findActiveTokensForSellers();
            case "VENDORS" -> userDeviceTokenRepository.findActiveTokensForVendors();
            case "TEST" -> {
                if (filter != null && filter.containsKey("fcmToken")) {
                    yield List.of((String) filter.get("fcmToken"));
                }
                yield List.of();
            }
            default -> List.of();
        };
    }

    private PushTemplateResponse mapToTemplateResponse(PushTemplate t) {
        return PushTemplateResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .body(t.getBody())
                .imageUrl(t.getImageUrl())
                .deepLink(t.getDeepLink())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private PushCampaignResponse mapToCampaignResponse(PushCampaign c) {
        return PushCampaignResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .status(c.getStatus())
                .targetedCount(c.getTargetedCount())
                .successCount(c.getSuccessCount())
                .failureCount(c.getFailureCount())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
