package com.beta.controller.user.request;

import com.beta.account.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 사용자 통계 조회 조건")
public record AdminUserStatisticsRequest(
        @Schema(description = "사용자 상태 필터", example = "ACTIVE", nullable = true)
        User.UserStatus status
) {
}
