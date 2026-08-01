package com.landgo.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full campaign view for the admin detail / preview screen.
 *
 * <p>{@code htmlBody} is the decoded, ready-to-render HTML that was actually emailed — the admin
 * portal does not need to Base64-decode it before previewing.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsletterCampaignDetailResponse {
    private UUID id;
    private String subject;
    private String htmlBody;
    private String textBody;
    private String previewText;
    private Integer recipientCount;
    private Integer successCount;
    private Integer failureCount;
    private String status;
    private Integer attemptCount;
    /** Populated when {@code status} is FAILED. */
    private String errorMessage;
    private UUID sentBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
