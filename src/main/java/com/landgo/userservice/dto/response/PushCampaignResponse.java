package com.landgo.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PushCampaignResponse {
    private UUID id;
    private String title;
    private String status;
    private Integer targetedCount;
    private Integer successCount;
    private Integer failureCount;
    private LocalDateTime createdAt;
}
