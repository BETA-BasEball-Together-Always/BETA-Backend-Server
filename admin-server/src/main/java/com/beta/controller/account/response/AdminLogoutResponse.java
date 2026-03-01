package com.beta.controller.account.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 로그아웃 응답")
public record AdminLogoutResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success,
        @Schema(description = "응답 메시지", example = "로그아웃이 완료되었습니다.")
        String message
) {

    public static AdminLogoutResponse ofSuccess() {
        return new AdminLogoutResponse(true, "로그아웃이 완료되었습니다.");
    }
}
