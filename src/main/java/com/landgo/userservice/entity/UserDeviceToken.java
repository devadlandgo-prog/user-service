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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_device_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceToken {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", nullable = false, unique = true)
    private String fcmToken;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_seen_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastSeenAt = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
