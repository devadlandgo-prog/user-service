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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "push_delivery_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushDeliveryLog {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "provider_msg_id")
    private String providerMsgId;

    @Column(name = "error_code")
    private String errorCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
