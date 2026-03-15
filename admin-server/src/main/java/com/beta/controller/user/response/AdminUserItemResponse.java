package com.beta.controller.user.response;

import com.beta.account.application.admin.dto.AdminUserQueryResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 사용자 조회 항목")
public record AdminUserItemResponse(
        @Schema(description = "사용자 ID", example = "2")
        Long userId,
        @Schema(description = "닉네임", example = "slugger2")
        String nickname,
        @Schema(description = "이메일", example = "user2@test.com")
        String email,
        @Schema(description = "가입일시", example = "2026-03-15T12:30:00")
        LocalDateTime joinedAt,
        @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
        SocialProvider socialProvider,
        @Schema(description = "응원 팀명", example = "LG 트윈스", nullable = true)
        String favoriteTeamName,
        @Schema(description = "성별", example = "M", nullable = true)
        User.GenderType gender,
        @Schema(description = "나이", example = "28", nullable = true)
        Integer age,
        @Schema(description = "소개글", example = "LG 팬입니다", nullable = true)
        String bio,
        @Schema(description = "사용자 상태", example = "ACTIVE")
        User.UserStatus status
) {
    public static AdminUserItemResponse from(AdminUserQueryResult result) {
        return new AdminUserItemResponse(
                result.userId(),
                result.nickname(),
                result.email(),
                result.joinedAt(),
                result.socialProvider(),
                result.favoriteTeamName(),
                result.gender(),
                result.age(),
                result.bio(),
                result.status()
        );
    }
}
