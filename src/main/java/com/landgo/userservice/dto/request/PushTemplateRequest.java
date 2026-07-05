package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PushTemplateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 500, message = "Body cannot exceed 500 characters")
    private String body;

    private String imageUrl;
    private String deepLink;
}
