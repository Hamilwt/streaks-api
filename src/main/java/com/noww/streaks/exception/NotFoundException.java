package com.noww.streaks.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        this("NOT_FOUND", message);
    }

    public NotFoundException(String error, String message) {
        super(HttpStatus.NOT_FOUND, error, message);
    }
}
