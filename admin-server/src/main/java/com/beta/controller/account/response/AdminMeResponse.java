package com.beta.controller.account.response;

import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 관리자 정보 응답")
public record AdminMeResponse(
        @Schema(description = "관리자 사용자 ID", example = "1")
        Long userId,
        @Schema(description = "권한", example = "ADMIN")
        String role,
        @Schema(description = "토큰 클라이언트 구분값", example = "ADMIN")
        String client
) {

    public static AdminMeResponse from(CustomUserDetails userDetails) {
        return new AdminMeResponse(
                userDetails.userId(),
                userDetails.role(),
                userDetails.client()
        );
    }
}
