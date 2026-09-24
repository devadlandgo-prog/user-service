package com.landgo.userservice.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * The one place that decides how long a verification code is and how long it lives.
 *
 * <p>Email codes were four digits while phone codes came from Twilio Verify at six, so the same
 * sign-up asked for two different code lengths and the clients rendered the wrong number of input
 * boxes for one of them. Length is now a single product-wide setting used by generation,
 * validation and the {@code /auth/verification-policy} endpoint clients read to size their input.
 *
 * <p>The default is six because Twilio Verify's own service setting is six, and that side is
 * configured in the Twilio console rather than in this code. Moving to four means changing
 * {@code app.verification.code-length} <em>and</em> the Twilio Verify service, or the mismatch
 * simply reappears.
 */
@Slf4j
@Getter
@Component
public class VerificationCodePolicy {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 8;

    /** Digits in every numeric verification code, email and SMS alike. */
    @Value("${app.verification.code-length:6}")
    private int codeLength;

    /** Account-verification codes are short-lived; the email states ten minutes. */
    @Value("${app.verification.email-code-expiry-minutes:10}")
    private int emailCodeExpiryMinutes;

    /** Password-reset codes last longer; the email states sixty minutes. */
    @Value("${app.verification.reset-code-expiry-minutes:60}")
    private int resetCodeExpiryMinutes;

    @PostConstruct
    void validate() {
        if (codeLength < MIN_LENGTH || codeLength > MAX_LENGTH) {
            log.warn("app.verification.code-length={} is out of range [{}, {}]; falling back to 6",
                    codeLength, MIN_LENGTH, MAX_LENGTH);
            codeLength = 6;
        }
        log.info("Verification codes: {} digits, account codes expire in {} min, reset codes in {} min",
                codeLength, emailCodeExpiryMinutes, resetCodeExpiryMinutes);
    }

    /**
     * A zero-padded numeric code of exactly {@link #getCodeLength()} digits.
     *
     * <p>Padded rather than range-shifted so every code really is the configured length — an
     * unpadded random draw produces occasional short codes, which is exactly the class of bug
     * this policy exists to remove.
     */
    public String generateNumericCode() {
        int bound = (int) Math.pow(10, codeLength);
        return String.format("%0" + codeLength + "d", SECURE_RANDOM.nextInt(bound));
    }

    /** Whether a submitted code is the right shape, before it is compared to anything stored. */
    public boolean isWellFormed(String code) {
        return code != null && code.length() == codeLength && code.chars().allMatch(Character::isDigit);
    }
}
