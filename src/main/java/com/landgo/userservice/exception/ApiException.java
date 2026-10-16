package com.landgo.userservice.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    /**
     * Machine-readable context for the failure, surfaced as the response's {@code details}.
     *
     * <p>Null for most errors, where the code and message are the whole story. A client that has to
     * correct the request needs the numbers it broke — an expertise selection rejected for
     * exceeding a plan allowance has to say what the allowance was.
     */
    private final Object details;

    public ApiException(String message, HttpStatus status, String code) {
        this(message, status, code, null);
    }

    public ApiException(String message, HttpStatus status, String code, Object details) {
        super(message); this.status = status; this.code = code; this.details = details;
    }
}
