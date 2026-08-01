package com.landgo.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "push_campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushCampaign {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "deep_link")
    private String deepLink;

    @Column(name = "audience", nullable = false)
    private String audience;

    @Column(name = "audience_filter", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> audienceFilter;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "targeted_count", nullable = false)
    @Builder.Default
    private Integer targetedCount = 0;

    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "failure_count", nullable = false)
    @Builder.Default
    private Integer failureCount = 0;

    /** Failure reason surfaced to the admin portal when {@code status} is FAILED. */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Times the worker has claimed this campaign; caps retries of stale in-flight jobs. */
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "sent_by")
    private UUID sentBy;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
