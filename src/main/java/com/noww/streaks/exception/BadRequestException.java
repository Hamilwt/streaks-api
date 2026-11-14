package com.noww.streaks.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        this("BAD_REQUEST", message);
    }

    public BadRequestException(String error, String message) {
        super(HttpStatus.BAD_REQUEST, error, message);
    }
}
