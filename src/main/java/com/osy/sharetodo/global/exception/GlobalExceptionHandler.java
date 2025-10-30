package com.osy.sharetodo.global.exception;

import com.osy.sharetodo.global.response.ApiResponse;
import com.osy.sharetodo.global.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handle(ApiException e) {
        return ResponseEntity.status(e.code().getStatus())
                .body(ApiResponse.error(e.code().getDefaultMessage(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handle(MethodArgumentNotValidException e) {
        var fe = e.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "Validation error";
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getDefaultMessage(), msg));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildResponse(ErrorCode.CONFLICT, "데이터 무결성 제약 조건에 위배되었습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        ex.printStackTrace();
        return buildResponse(ErrorCode.INTERNAL_ERROR, "알 수 없는 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String message) {
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message);
        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }
}
