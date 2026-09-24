package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One attempted transactional email, keyed by the business event that caused it.
 *
 * <p>Every service that sends mail goes through user-service, so deduplication lives here rather
 * than being reimplemented per caller. A Stripe webhook replay, a retried
 * {@code verify-and-fulfill} or a re-delivered queue message all arrive with the same
 * {@link #idempotencyKey} and are recognised as the same send.
 *
 * <p>A user action that legitimately repeats — asking for another password reset, say — supplies a
 * fresh key, so it is a new event and mails again.
 */
@Entity
@Table(
        name = "email_deliveries",
        uniqueConstraints = @UniqueConstraint(
                name = "email_deliveries_idempotency_key_key",
                columnNames = {"idempotency_key"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDelivery {

    public enum Status { PENDING, SENT, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 200)
    private String idempotencyKey;

    @Column(name = "to_email", nullable = false, length = 320)
    private String toEmail;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    /** Provider failure text. Never holds the message body, an OTP or a token. */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
