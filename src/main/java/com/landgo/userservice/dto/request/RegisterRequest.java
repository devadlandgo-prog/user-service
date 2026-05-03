package com.landgo.userservice.dto.request;

import com.landgo.userservice.enums.AuthProvider;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @Size(max = 100) private String fullName;
    @NotBlank(message = "First name is required") @Size(max = 50) private String firstName;
    @NotBlank(message = "Last name is required") @Size(max = 50) private String lastName;
    @NotBlank(message = "Email is required") @Email private String email;
    @NotBlank(message = "Role is required (buyer, seller, professional, admin)") private String role;
    @NotBlank(message = "Password is required") @Size(min = 8) private String password;
    @NotBlank(message = "Confirm password is required") private String confirmPassword;
    private String phone;
    @Size(max = 200) private String agencyName;
    @Size(max = 50) private String licenseNumber;
    @Builder.Default private AuthProvider authProvider = AuthProvider.EMAIL;
    private String providerId;
    private String profileImageUrl;
}
