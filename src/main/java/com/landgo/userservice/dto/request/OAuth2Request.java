package com.landgo.userservice.dto.request;

import com.landgo.userservice.enums.AuthProvider;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OAuth2Request {
    @NotBlank private String token;
    @NotNull private AuthProvider authProvider;
}
