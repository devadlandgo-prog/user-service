package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "email_verification_tokens")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "code", nullable = false, length = 6) private String code;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "expiry_date", nullable = false) private LocalDateTime expiryDate;
    @Builder.Default @Column(name = "used", nullable = false) private boolean used = false;
    @Builder.Default @Column(name = "attempts", nullable = false) private int attempts = 0;
    @Column(name = "created_at", nullable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiryDate); }
    public void incrementAttempts() { this.attempts++; }
}
