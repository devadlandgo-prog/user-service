package com.landgo.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "newsletter_campaign_recipients", schema = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterCampaignRecipient extends BaseEntity {

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "subscriber_id", nullable = false)
    private UUID subscriberId;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // SENT, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
