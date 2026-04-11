package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank private String token;
    @NotBlank @Size(min = 8) private String password;
    @NotBlank private String confirmPassword;
}
