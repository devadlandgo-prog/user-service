package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaResendRequest {
    @NotBlank
    private String mfaSession;
}
