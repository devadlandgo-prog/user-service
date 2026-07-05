package com.landgo.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PushTemplateResponse {
    private UUID id;
    private String title;
    private String body;
    private String imageUrl;
    private String deepLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
