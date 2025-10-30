package com.osy.sharetodo.global.response;

import com.osy.sharetodo.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private int status;
    private String code;
    private String message;

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.status = errorCode.getStatus().value();
        this.code = errorCode.name();
        this.message = message;
    }
}