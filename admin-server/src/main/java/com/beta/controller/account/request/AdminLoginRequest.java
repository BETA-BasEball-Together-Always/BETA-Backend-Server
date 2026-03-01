package com.beta.controller.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 카카오 로그인 요청")
public record AdminLoginRequest(
        @Schema(description = "카카오 액세스 토큰", example = "kakao-access-token", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "토큰은 필수입니다.")
        String token
) {
}
