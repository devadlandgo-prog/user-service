package com.landgo.userservice.dto.request;

import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.UserType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotNull(message = "User type is required (SELLER or AGENT)") private UserType userType;
    @NotBlank(message = "Full name is required") @Size(min = 2, max = 100) private String fullName;
    @NotBlank(message = "Email is required") @Email private String email;
    @NotBlank(message = "Password is required") @Size(min = 8) private String password;
    @NotBlank(message = "Confirm password is required") private String confirmPassword;
    private String phone;
    @Size(max = 200) private String agencyName;
    @Size(max = 50) private String recoLicenseNumber;
    private Boolean agentAuthorizationAccepted;
    @Builder.Default private AuthProvider authProvider = AuthProvider.EMAIL;
    private String providerId;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
}
