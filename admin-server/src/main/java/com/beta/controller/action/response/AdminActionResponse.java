package com.beta.controller.action.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 액션 응답")
public record AdminActionResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success,
        @Schema(description = "응답 메시지", example = "회원이 정지되었습니다.")
        String message
) {

    public static AdminActionResponse success(String message) {
        return new AdminActionResponse(true, message);
    }
}
