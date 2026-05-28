package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ForgotPasswordVerifyRequest {
    @NotBlank private String emailOrPhone;
    @NotBlank @Pattern(regexp = "^[0-9]{4}$", message = "Code must be 4 digits") private String code;
}
