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

import java.util.UUID;

@Entity
@Table(name = "newsletter_subscribers", schema = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "consent", nullable = false)
    private boolean consent;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // ACTIVE, UNSUBSCRIBED

    /**
     * Opaque per-subscriber token used in the one-click unsubscribe link carried by every
     * newsletter, so the address itself never has to travel in a query string.
     */
    @Column(name = "unsubscribe_token", nullable = false, unique = true)
    @Builder.Default
    private UUID unsubscribeToken = UUID.randomUUID();
}
