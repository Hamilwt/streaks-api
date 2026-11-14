package com.noww.streaks.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        this("CONFLICT", message);
    }

    public ConflictException(String error, String message) {
        super(HttpStatus.CONFLICT, error, message);
    }
}
