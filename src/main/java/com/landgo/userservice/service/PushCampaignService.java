package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.PushCampaignRequest;
import com.landgo.userservice.dto.request.PushTemplateRequest;
import com.landgo.userservice.dto.response.PushCampaignDetailResponse;
import com.landgo.userservice.dto.response.PushCampaignResponse;
import com.landgo.userservice.dto.response.PushTemplateResponse;
import com.landgo.userservice.entity.PushCampaign;
import com.landgo.userservice.entity.PushTemplate;
import com.landgo.userservice.enums.CampaignStatus;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.repository.PushCampaignRepository;
import com.landgo.userservice.repository.PushTemplateRepository;
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
    private final PushAudienceResolver audienceResolver;

    // --- templates ---

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
                .orElseThrow(() -> new ResourceNotFoundException("PushTemplate", "id", id));

        template.setTitle(request.getTitle());
        template.setBody(request.getBody());
        template.setImageUrl(request.getImageUrl());
        template.setDeepLink(request.getDeepLink());

        return mapToTemplateResponse(pushTemplateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        if (!pushTemplateRepository.existsById(id)) {
            throw new ResourceNotFoundException("PushTemplate", "id", id);
        }
        pushTemplateRepository.deleteById(id);
    }

    // --- campaigns ---

    public Page<PushCampaignResponse> getCampaigns(Pageable pageable) {
        return pushCampaignRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToCampaignResponse);
    }

    public PushCampaignDetailResponse getCampaign(UUID id) {
        PushCampaign campaign = pushCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PushCampaign", "id", id));
        return mapToCampaignDetailResponse(campaign);
    }

    @Transactional
    public PushCampaignResponse createCampaign(UUID adminUserId, PushCampaignRequest request) {
        // Resolved up front purely so the admin sees the audience size immediately; the worker
        // resolves again at send time, and that later count is the one reported as targetedCount.
        int estimatedTargets = audienceResolver.resolve(request.getAudience(), request.getAudienceFilter()).size();

        PushCampaign campaign = PushCampaign.builder()
                .templateId(request.getTemplateId())
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .deepLink(request.getDeepLink())
                .audience(request.getAudience())
                .audienceFilter(request.getAudienceFilter())
                .status(resolveInitialStatus(request))
                .targetedCount(estimatedTargets)
                .sentBy(adminUserId)
                .scheduledAt(request.getScheduledAt())
                .build();

        PushCampaign saved = pushCampaignRepository.save(campaign);
        log.info("Created push campaign {} with status {} and ~{} target(s)",
                saved.getId(), saved.getStatus(), estimatedTargets);

        return mapToCampaignResponse(saved);
    }

    /**
     * A campaign is queued for immediate pickup, parked until its scheduled time, or left as a
     * draft when neither was requested.
     */
    private String resolveInitialStatus(PushCampaignRequest request) {
        LocalDateTime scheduledAt = request.getScheduledAt();
        if (scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now())) {
            return CampaignStatus.SCHEDULED;
        }
        return request.isSendNow() ? CampaignStatus.QUEUED : CampaignStatus.DRAFT;
    }

    // --- mapping ---

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
                .scheduledAt(c.getScheduledAt())
                .completedAt(c.getCompletedAt())
                .errorMessage(c.getErrorMessage())
                .build();
    }

    private PushCampaignDetailResponse mapToCampaignDetailResponse(PushCampaign c) {
        return PushCampaignDetailResponse.builder()
                .id(c.getId())
                .templateId(c.getTemplateId())
                .title(c.getTitle())
                .body(c.getBody())
                .imageUrl(c.getImageUrl())
                .deepLink(c.getDeepLink())
                .audience(c.getAudience())
                .audienceFilter(c.getAudienceFilter())
                .status(c.getStatus())
                .targetedCount(c.getTargetedCount())
                .successCount(c.getSuccessCount())
                .failureCount(c.getFailureCount())
                .attemptCount(c.getAttemptCount())
                .errorMessage(c.getErrorMessage())
                .sentBy(c.getSentBy())
                .scheduledAt(c.getScheduledAt())
                .startedAt(c.getStartedAt())
                .completedAt(c.getCompletedAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
