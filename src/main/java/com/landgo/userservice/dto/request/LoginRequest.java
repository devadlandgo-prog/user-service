package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    @NotBlank private String identifier;
    @NotBlank private String password;
}
