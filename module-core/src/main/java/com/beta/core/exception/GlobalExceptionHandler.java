package com.beta.core.exception;

import com.beta.core.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     * BaseException을 상속받은 모든 커스텀 예외 처리
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception occurred: code={}, message={}", errorCode.getCode(), e.getMessage());

        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * Bean Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.of(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus()).body(errorResponse);
    }

    /**
     * Form 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.warn("Bind exception occurred: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.of(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus()).body(errorResponse);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: field={}, rejectedValue={}", e.getName(), e.getValue());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * HTTP 메서드 불일치 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not allowed: {}", e.getMethod());
        ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 예상하지 못한 예외 처리 (Fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
