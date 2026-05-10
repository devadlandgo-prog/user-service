package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Login request payload")
public class LoginRequest {
    @NotBlank 
    @Schema(description = "Email address or phone number", example = "john@example.com")
    private String emailOrPhone;
    
    @NotBlank 
    @Schema(example = "yourpassword")
    private String password;
}
