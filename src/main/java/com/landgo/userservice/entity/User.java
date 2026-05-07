package com.landgo.userservice.entity;

import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity @Table(name = "users")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {
    @Enumerated(EnumType.STRING) @Column(name = "user_type", nullable = false, length = 20) @Builder.Default
    private UserType userType = UserType.SELLER;
    @Column(name = "full_name", nullable = false, length = 100) private String fullName;
    @Column(name = "first_name", length = 50) private String firstName;
    @Column(name = "last_name", length = 50) private String lastName;
    @Column(name = "email", nullable = false, unique = true) private String email;
    @Column(name = "password") private String password;
    @Column(name = "phone", length = 20) private String phone;
    @Column(name = "profile_image_url", length = 500) private String profileImageUrl;
    @Column(name = "location", length = 200) private String location;
    @Column(name = "professional_bio", columnDefinition = "TEXT") private String professionalBio;
    @Enumerated(EnumType.STRING) @Column(name = "auth_provider", nullable = false, length = 20) @Builder.Default
    private AuthProvider authProvider = AuthProvider.EMAIL;
    @Column(name = "provider_id") private String providerId;
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false, length = 20) @Builder.Default
    private Role role = Role.SELLER;
    @Column(name = "email_verified") @Builder.Default private boolean emailVerified = false;
    @Column(name = "email_verified_at") private LocalDateTime emailVerifiedAt;
    @Column(name = "active") @Builder.Default private boolean active = true;
    @Column(name = "agency_name", length = 200) private String agencyName;
    @Column(name = "reco_license_number", length = 50) private String recoLicenseNumber;
    @Column(name = "agent_authorization_accepted") @Builder.Default private boolean agentAuthorizationAccepted = false;
    @Column(name = "mfa_enabled") @Builder.Default private boolean mfaEnabled = false;
    @Column(name = "mfa_verified") @Builder.Default private boolean mfaVerified = false;
    @Column(name = "timezone", length = 50) private String timezone;
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) return fullName;
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    public boolean isVendor() { return role == Role.VENDOR; }
    public boolean isAgent() { return userType == UserType.AGENT || role == Role.AGENT; }
}
