package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VerifyEmailRequest {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{4}$", message = "Code must be 4 digits") private String code;
    private String type; // e.g. "email" or "mfa"
}
