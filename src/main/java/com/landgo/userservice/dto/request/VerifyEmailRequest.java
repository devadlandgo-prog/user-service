package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VerifyEmailRequest {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{4,8}$", message = "Verification code must be 4 to 8 digits") private String code;
    private String type; // e.g. "email" or "mfa"
}
