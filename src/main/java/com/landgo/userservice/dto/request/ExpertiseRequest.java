package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExpertiseRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private Boolean active;
}
