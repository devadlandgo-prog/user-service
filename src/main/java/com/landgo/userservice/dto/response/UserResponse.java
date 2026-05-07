package com.landgo.userservice.dto.response;

import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.enums.UserType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private UUID id;
    private UserType userType;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String location;
    private String professionalBio;
    private AuthProvider authProvider;
    private Role role;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private boolean active;
    private String agencyName;
    private String recoLicenseNumber;
    private boolean isVendor;
    private boolean isAgent;
    private boolean mfaEnabled;
    private boolean mfaVerified;
    private String timezone;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
