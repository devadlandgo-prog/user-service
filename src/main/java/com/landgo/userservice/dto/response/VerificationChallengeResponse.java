package com.landgo.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Tells a client how to render a code-entry screen.
 *
 * <p>Returned by every endpoint that issues a code, so mobile and web size their input boxes from
 * the server's configuration instead of a hardcoded four or six.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shape and lifetime of the verification code that was just issued")
public class VerificationChallengeResponse {

    @Schema(example = "6", description = "Number of digits in the code")
    private int codeLength;

    @Schema(example = "10", description = "Minutes until the issued code expires")
    private int expiresInMinutes;

    @Schema(example = "EMAIL", description = "EMAIL, SMS, or BOTH when a code was sent to each")
    private String channel;
}
