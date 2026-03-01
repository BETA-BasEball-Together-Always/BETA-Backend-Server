package com.beta.controller.account.response;

import com.beta.account.application.admin.dto.AdminLoginResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 로그인 응답")
public record AdminLoginResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "관리자 사용자 정보")
        AdminUser user
) {

    public static AdminLoginResponse from(AdminLoginResult result) {
        return new AdminLoginResponse(
                result.accessToken(),
                AdminUser.from(result)
        );
    }

    @Schema(description = "관리자 사용자 요약 정보")
    public record AdminUser(
            @Schema(description = "관리자 사용자 ID", example = "1")
            Long userId,
            @Schema(description = "이메일", example = "admin@test.com")
            String email,
            @Schema(description = "닉네임", example = "admin_user")
            String nickname,
            @Schema(description = "권한", example = "ADMIN")
            String role
    ) {

        public static AdminUser from(AdminLoginResult result) {
            return new AdminUser(
                    result.userId(),
                    result.email(),
                    result.nickname(),
                    result.role()
            );
        }
    }
}
