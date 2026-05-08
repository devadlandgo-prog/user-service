package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "type", length = 50)
    private String type; // e.g., "SYSTEM", "INQUIRY", "LISTING"
}
