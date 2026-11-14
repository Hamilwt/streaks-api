package com.noww.streaks.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException {

    public UnprocessableEntityException(String message) {
        this("UNPROCESSABLE_ENTITY", message);
    }

    public UnprocessableEntityException(String error, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, error, message);
    }
}
