package com.landgo.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** Full campaign view, including the message body and audience, for the admin detail screen. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushCampaignDetailResponse {
    private UUID id;
    private UUID templateId;
    private String title;
    private String body;
    private String imageUrl;
    private String deepLink;
    private String audience;
    private Map<String, Object> audienceFilter;
    private String status;
    private Integer targetedCount;
    private Integer successCount;
    private Integer failureCount;
    private Integer attemptCount;
    private String errorMessage;
    private UUID sentBy;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
