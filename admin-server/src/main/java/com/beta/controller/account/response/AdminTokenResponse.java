package com.beta.controller.account.response;

import com.beta.account.application.admin.dto.AdminTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 토큰 응답")
public record AdminTokenResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {

    public static AdminTokenResponse from(AdminTokenResult result) {
        return new AdminTokenResponse(result.accessToken());
    }
}
