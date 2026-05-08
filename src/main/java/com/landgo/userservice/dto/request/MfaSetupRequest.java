package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaSetupRequest {
    @NotBlank
    private String phone;
    private boolean enabled;
}
