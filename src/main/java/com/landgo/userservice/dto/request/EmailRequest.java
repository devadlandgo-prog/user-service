package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    @NotBlank(message = "Recipient email is required")
    private String toEmail;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Template name is required")
    private String templateName;

    private Map<String, String> variables;
}
