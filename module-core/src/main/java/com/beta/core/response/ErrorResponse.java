package com.beta.core.response;

import com.beta.core.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "USER001")
    private String code;

    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "에러 발생 시각", example = "2025-01-01T00:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "필드 에러 목록")
    private List<FieldError> errors;

    private ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode, errors);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(description = "필드 에러 상세")
    public static class FieldError {
        @Schema(description = "필드명", example = "email")
        private String field;

        @Schema(description = "에러 메시지", example = "이메일 형식이 올바르지 않습니다.")
        private String message;

        @Schema(description = "거부된 값", example = "invalid-email")
        private Object rejectedValue;

        private FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public static FieldError of(String field, String message, Object rejectedValue) {
            return new FieldError(field, message, rejectedValue);
        }
    }
}
