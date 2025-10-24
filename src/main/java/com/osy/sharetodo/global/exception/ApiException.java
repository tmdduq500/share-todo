package com.osy.sharetodo.global.exception;

public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.errorCode = code;
    }

    public ErrorCode code() {
        return errorCode;
    }
}