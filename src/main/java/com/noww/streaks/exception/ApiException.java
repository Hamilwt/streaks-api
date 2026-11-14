package com.noww.streaks.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for errors that should be rendered as a JSON {@code ErrorResponse}
 * with a specific HTTP status.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    protected ApiException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
