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

    private String templateName;

    private Map<String, String> variables;

    private String htmlBody;

    /**
     * Key identifying the committed business event this email belongs to.
     *
     * <p>Callers must reuse it when retrying so a replay delivers nothing. Omitting it sends
     * undeduplicated, which is only correct for genuinely one-off mail.
     */
    private String idempotencyKey;

    /** Originating domain event id, recorded alongside the delivery attempt for tracing. */
    private String eventId;
}
