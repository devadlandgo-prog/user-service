package com.landgo.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** Summary view used by the admin campaign history list. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushCampaignResponse {
    private UUID id;
    private String title;
    private String status;
    private Integer targetedCount;
    private Integer successCount;
    private Integer failureCount;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    /** Populated when {@code status} is FAILED. */
    private String errorMessage;
}
