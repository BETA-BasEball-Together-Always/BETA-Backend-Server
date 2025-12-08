package com.beta.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "C001", "Validation failed"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method not allowed"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "Invalid type value"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "Entity not found"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "Access denied"),

    // Authentication & Authorization
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "Token expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Invalid token"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "Email already exists"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "Invalid password"),

    // Community
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Post not found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "Comment not found");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
