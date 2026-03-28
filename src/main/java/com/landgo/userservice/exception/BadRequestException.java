package com.landgo.userservice.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) { super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST"); }
    public BadRequestException(String message, String code) { super(message, HttpStatus.BAD_REQUEST, code); }
}
