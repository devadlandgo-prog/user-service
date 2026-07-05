package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PushCampaignRequest {
    private UUID templateId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private String imageUrl;
    
    private String deepLink;

    @NotBlank(message = "Audience is required")
    private String audience;

    private Map<String, Object> audienceFilter;

    private boolean sendNow = true;

    private LocalDateTime scheduledAt;
}
