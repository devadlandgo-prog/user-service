package com.landgo.userservice.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidationSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .role("buyer")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected no violations");
    }

    @Test
    public void testValidationFailure() {
        RegisterRequest request = RegisterRequest.builder().build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Expected violations for empty request");
        
        // Check if firstName and lastName are in violations
        boolean hasFirstName = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        boolean hasLastName = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
        
        assertTrue(hasFirstName, "Expected violation for firstName");
        assertTrue(hasLastName, "Expected violation for lastName");
    }
}
