package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ForgotPasswordRequest {
    @NotBlank private String emailOrPhone;
}
