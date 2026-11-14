package com.noww.streaks.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        this("UNAUTHORIZED", message);
    }

    public UnauthorizedException(String error, String message) {
        super(HttpStatus.UNAUTHORIZED, error, message);
    }
}
