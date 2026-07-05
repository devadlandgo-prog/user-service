package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewsletterPublishRequest {
    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "HTML body is required")
    private String htmlBody;

    private String textBody;

    private String previewText;
}
