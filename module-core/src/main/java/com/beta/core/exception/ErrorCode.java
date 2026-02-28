package com.beta.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION001", "입력값 검증에 실패했습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER001", "서버 내부 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON001", "지원하지 않는 HTTP 메서드입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON002", "잘못된 타입 값입니다"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON003", "요청한 리소스를 찾을 수 없습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON004", "접근 권한이 없습니다"),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT001", "토큰이 만료되었습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT002", "유효하지 않은 토큰입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,  "JWT003", "토큰 처리 중 오류가 발생했습니다"),

    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "SOCIAL001", "유효하지 않은 소셜 로그인 토큰입니다"),
    SOCIAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SOCIAL002", "소셜 로그인 API 호출 중 오류가 발생했습니다"),
    INVALID_APPLE_TOKEN(HttpStatus.UNAUTHORIZED, "SOCIAL003", "유효하지 않은 Apple IdToken입니다"),
    SOCIAL_EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "SOCIAL004", "소셜 계정에 등록된 이메일이 없습니다"),

    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM001", "해당 구단은 존재하지 않습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다"),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "USER002", "탈퇴한 사용자입니다"),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "USER003", "정지된 사용자입니다"),
    NAME_DUPLICATE(HttpStatus.CONFLICT, "USER004", "이미 존재하는 이름입니다"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT,"USER006", "이미 존재하는 이메일입니다"),
    PERSONAL_INFO_AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "USER007", "개인정보 수집 및 이용에 대한 필수 동의가 필요합니다"),
    INVALID_SIGNUP_STEP(HttpStatus.BAD_REQUEST, "USER008", "잘못된 회원가입 단계입니다"),
    INVALID_NICKNAME_LENGTH(HttpStatus.BAD_REQUEST, "USER009", "닉네임은 2-13자 사이여야 합니다"),

    ADMIN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "ADMIN001", "관리자 권한의 사용자가 아닙니다."),

    INVALID_CHANNEL_ACCESS(HttpStatus.FORBIDDEN, "COMMUNITY001", "채널 접근 권한이 없습니다"),
    INVALID_IMAGE(HttpStatus.BAD_REQUEST, "COMMUNITY002", "유효하지 않은 이미지입니다"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMUNITY003", "이미지 업로드에 실패했습니다"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY004", "게시글을 찾을 수 없습니다"),
    DUPLICATE_POST(HttpStatus.CONFLICT, "COMMUNITY005", "동일한 내용의 게시글이 최근에 작성되었습니다"),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMUNITY006", "게시글에 대한 권한이 없습니다"),
    POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COMMUNITY007", "이미 삭제된 게시글입니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY008", "댓글을 찾을 수 없습니다"),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMUNITY009", "댓글에 대한 권한이 없습니다"),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COMMUNITY010", "이미 삭제된 댓글입니다"),
    COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMUNITY011", "답글은 1단계까지만 가능합니다"),
    DUPLICATE_COMMENT(HttpStatus.CONFLICT, "COMMUNITY012", "동일한 내용의 댓글이 최근에 작성되었습니다"),
    SELF_BLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMUNITY013", "자기 자신을 차단할 수 없습니다"),
    ALREADY_BLOCKED(HttpStatus.CONFLICT, "COMMUNITY014", "이미 차단된 사용자입니다"),
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY015", "차단 정보를 찾을 수 없습니다"),
    BLOCKED_USER_PROFILE(HttpStatus.FORBIDDEN, "COMMUNITY016", "차단한 사용자의 프로필을 조회할 수 없습니다"),

    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "SEARCH001", "커서의 score, id는 둘다 존재하거나 둘다 비어있어야 합니다."),
    SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SEARCH099", "es 검색 중 오류가 발생했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
