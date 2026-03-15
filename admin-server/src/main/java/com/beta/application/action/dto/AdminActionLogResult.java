package com.beta.application.action.dto;

import com.beta.domain.entity.AdminLog;
import com.beta.domain.entity.AdminLogAction;
import com.beta.domain.entity.AdminLogTargetType;

import java.time.LocalDateTime;

public record AdminActionLogResult(
        Long logId,
        Long actorAdminId,
        String actorAdminNickname,
        AdminLogTargetType targetType,
        Long targetId,
        AdminLogAction action,
        String beforeStatus,
        String afterStatus,
        String reason,
        LocalDateTime createdAt
) {
    public static AdminActionLogResult from(AdminLog adminLog, String actorAdminNickname) {
        return new AdminActionLogResult(
                adminLog.getId(),
                adminLog.getActorAdminId(),
                actorAdminNickname,
                adminLog.getTargetType(),
                adminLog.getTargetId(),
                adminLog.getAction(),
                adminLog.getBeforeStatus(),
                adminLog.getAfterStatus(),
                adminLog.getReason(),
                adminLog.getCreatedAt()
        );
    }
}
