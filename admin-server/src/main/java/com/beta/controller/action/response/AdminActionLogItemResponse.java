package com.beta.controller.action.response;

import com.beta.application.action.dto.AdminActionLogResult;
import com.beta.domain.entity.AdminLogAction;
import com.beta.domain.entity.AdminLogTargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 로그 항목 응답")
public record AdminActionLogItemResponse(
        @Schema(description = "로그 ID", example = "1")
        Long logId,
        @Schema(description = "액션을 수행한 관리자 ID", example = "1")
        Long actorAdminId,
        @Schema(description = "액션을 수행한 관리자 닉네임", example = "admin-user", nullable = true)
        String actorAdminNickname,
        @Schema(description = "대상 타입", example = "MEMBER")
        AdminLogTargetType targetType,
        @Schema(description = "대상 ID", example = "2")
        Long targetId,
        @Schema(description = "관리자 액션", example = "MEMBER_SUSPEND")
        AdminLogAction action,
        @Schema(description = "변경 전 상태", example = "ACTIVE", nullable = true)
        String beforeStatus,
        @Schema(description = "변경 후 상태", example = "SUSPENDED", nullable = true)
        String afterStatus,
        @Schema(description = "사유", example = "운영 정책 위반", nullable = true)
        String reason,
        @Schema(description = "로그 생성 시각", example = "2026-03-14T15:30:00")
        LocalDateTime createdAt
) {
    public static AdminActionLogItemResponse from(AdminActionLogResult result) {
        return new AdminActionLogItemResponse(
                result.logId(),
                result.actorAdminId(),
                result.actorAdminNickname(),
                result.targetType(),
                result.targetId(),
                result.action(),
                result.beforeStatus(),
                result.afterStatus(),
                result.reason(),
                result.createdAt()
        );
    }
}
