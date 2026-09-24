package com.landgo.userservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The product rule under test: one code length, applied everywhere.
 *
 * <p>Email codes were four digits and Twilio Verify's SMS codes six, so a single sign-up asked
 * for two different lengths and clients rendered the wrong number of boxes for one of them.
 */
class VerificationCodePolicyTest {

    private VerificationCodePolicy policyWith(int codeLength) {
        VerificationCodePolicy policy = new VerificationCodePolicy();
        ReflectionTestUtils.setField(policy, "codeLength", codeLength);
        ReflectionTestUtils.setField(policy, "emailCodeExpiryMinutes", 10);
        ReflectionTestUtils.setField(policy, "resetCodeExpiryMinutes", 60);
        policy.validate();
        return policy;
    }

    @Test
    @DisplayName("every generated code has exactly the configured number of digits")
    void generatesExactLength() {
        for (int length : new int[] {4, 6, 8}) {
            VerificationCodePolicy policy = policyWith(length);
            // Many draws: an unpadded random draw only produces a short code occasionally, so a
            // single sample would pass against the bug this guards.
            for (int i = 0; i < 2_000; i++) {
                String code = policy.generateNumericCode();
                assertEquals(length, code.length(), "code was '" + code + "'");
                assertTrue(code.chars().allMatch(Character::isDigit), "code was '" + code + "'");
            }
        }
    }

    @Test
    @DisplayName("codes can start with zero rather than being silently shortened")
    void allowsLeadingZeros() {
        VerificationCodePolicy policy = policyWith(4);
        boolean sawLeadingZero = false;
        for (int i = 0; i < 5_000 && !sawLeadingZero; i++) {
            sawLeadingZero = policy.generateNumericCode().startsWith("0");
        }
        assertTrue(sawLeadingZero, "expected at least one zero-padded code in 5000 draws");
    }

    @Test
    @DisplayName("a code of the wrong length or with non-digits is rejected")
    void rejectsMalformedCodes() {
        VerificationCodePolicy policy = policyWith(6);
        assertTrue(policy.isWellFormed("012345"));
        assertFalse(policy.isWellFormed("1234"));
        assertFalse(policy.isWellFormed("1234567"));
        assertFalse(policy.isWellFormed("12345a"));
        assertFalse(policy.isWellFormed(null));
    }

    @Test
    @DisplayName("an out-of-range configured length falls back to six rather than failing startup")
    void clampsAbsurdConfiguration() {
        assertEquals(6, policyWith(1).getCodeLength());
        assertEquals(6, policyWith(64).getCodeLength());
    }
}
