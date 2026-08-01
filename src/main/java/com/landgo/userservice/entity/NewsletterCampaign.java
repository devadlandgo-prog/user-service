package com.landgo.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "newsletter_campaigns", schema = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterCampaign extends BaseEntity {

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "html_body", nullable = false, columnDefinition = "TEXT")
    private String htmlBody;

    @Column(name = "text_body", columnDefinition = "TEXT")
    private String textBody;

    @Column(name = "preview_text")
    private String previewText;

    /** Subscribers targeted by this send. */
    @Column(name = "recipient_count", nullable = false)
    private int recipientCount;

    /** Recipients the email provider accepted. */
    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private int successCount = 0;

    /** Recipients the email provider rejected. */
    @Column(name = "failure_count", nullable = false)
    @Builder.Default
    private int failureCount = 0;

    /** SENT | PARTIAL | FAILED | NO_RECIPIENTS — see {@link com.landgo.userservice.enums.CampaignStatus}. */
    @Column(name = "status", nullable = false, length = 24)
    @Builder.Default
    private String status = "QUEUED";

    /** Set when the delivery worker claims the campaign; used to detect abandoned runs. */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Times the worker has claimed this campaign; caps retries of stale in-flight runs. */
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    /** Failure reason surfaced to the admin portal when {@code status} is FAILED. */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_by")
    private UUID sentBy;
}
