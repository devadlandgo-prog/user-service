package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank private String emailOrPhone;
    @NotBlank @Pattern(regexp = "^[0-9]{4,8}$", message = "Verification code must be 4 to 8 digits") private String code;
    @NotBlank @Size(min = 8) private String password;
    @NotBlank private String confirmPassword;
}
