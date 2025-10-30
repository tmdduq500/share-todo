package com.osy.sharetodo.global.exception;

import com.osy.sharetodo.global.response.ApiResponse;
import com.osy.sharetodo.global.response.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return buildResponse(ex.code(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");
        return buildResponse(ErrorCode.INVALID_INPUT, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildResponse(ErrorCode.CONFLICT, "데이터 무결성 제약 조건 위반");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        ex.printStackTrace();
        return buildResponse(ErrorCode.INTERNAL_ERROR, "알 수 없는 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String message) {
        ErrorResponse response = new ErrorResponse(errorCode, message);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}

