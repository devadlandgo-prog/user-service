package com.landgo.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary view for the admin newsletter history list.
 *
 * <p>Deliberately excludes {@code htmlBody} — the list would otherwise carry a full email
 * document per row. Use the detail endpoint for the body.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsletterCampaignResponse {
    private UUID id;
    private String subject;
    private String previewText;
    private Integer recipientCount;
    private Integer successCount;
    private Integer failureCount;
    private String status;
    /** Populated when {@code status} is FAILED. */
    private String errorMessage;
    private UUID sentBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
