package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity @Table(name = "notification_preferences")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class NotificationPreferences extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(name = "listing_alerts") @Builder.Default private boolean listingAlerts = true;
    @Column(name = "price_drops") @Builder.Default private boolean priceDrops = true;
    @Column(name = "marketing_emails") @Builder.Default private boolean marketingEmails = false;
    @Column(name = "security_alerts") @Builder.Default private boolean securityAlerts = true;
}
