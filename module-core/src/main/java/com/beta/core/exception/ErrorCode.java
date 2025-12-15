package com.beta.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION001", "입력값 검증에 실패했습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER001", "서버 내부 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON001", "지원하지 않는 HTTP 메서드입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON002", "잘못된 타입 값입니다"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON003", "요청한 리소스를 찾을 수 없습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON004", "접근 권한이 없습니다"),

    // Authentication & Authorization
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT001", "토큰이 만료되었습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT002", "유효하지 않은 토큰입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,  "JWT003", "토큰 처리 중 오류가 발생했습니다"),

    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "SOCIAL001", "유효하지 않은 소셜 로그인 토큰입니다"),
    SOCIAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SOCIAL002", "소셜 로그인 API 호출 중 오류가 발생했습니다"),

    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM001", "해당 구단은 존재하지 않습니다."),

    // Password
    PASSWORD_CODE_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS,"PASSWORD001", "인증코드 재전송은 1분 후에 가능합니다"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "PASSWORD002", "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다"),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "PASSWORD003", "인증코드가 일치하지 않습니다"),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "PASSWORD004", "인증코드가 만료되었습니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다"),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "USER002", "탈퇴한 사용자입니다"),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "USER003", "정지된 사용자입니다"),
    NAME_DUPLICATE(HttpStatus.CONFLICT, "USER004", "이미 존재하는 이름입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER005", "비밀번호가 일치하지 않습니다"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT,"USER006", "이미 존재하는 이메일입니다"),
    PERSONAL_INFO_AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "USER007", "개인정보 수집 및 이용에 대한 필수 동의가 필요합니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
