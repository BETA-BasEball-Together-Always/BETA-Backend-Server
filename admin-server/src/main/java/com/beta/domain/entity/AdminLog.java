package com.beta.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "admin_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLog extends BaseEntity {

    @Column(name = "actor_admin_id", nullable = false)
    private Long actorAdminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private AdminLogTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AdminLogAction action;

    @Column(name = "before_status", length = 20)
    private String beforeStatus;

    @Column(name = "after_status", length = 20)
    private String afterStatus;

    @Column(name = "reason", length = 255)
    private String reason;

    @Builder(access = AccessLevel.PRIVATE)
    private AdminLog(Long actorAdminId, Long targetId, AdminLogAction action, String reason) {
        validateId(actorAdminId, "actorAdminId");
        validateId(targetId, "targetId");

        this.actorAdminId = actorAdminId;
        this.targetId = targetId;
        this.targetType = action.getTargetType();
        this.action = action;
        this.beforeStatus = action.getBeforeStatus();
        this.afterStatus = action.getAfterStatus();
        this.reason = normalizeReason(reason);
    }

    public static AdminLog suspendMember(Long actorAdminId, Long targetMemberId, String reason) {
        return builder()
                .actorAdminId(actorAdminId)
                .targetId(targetMemberId)
                .action(AdminLogAction.MEMBER_SUSPEND)
                .reason(reason)
                .build();
    }

    public static AdminLog unsuspendMember(Long actorAdminId, Long targetMemberId, String reason) {
        return builder()
                .actorAdminId(actorAdminId)
                .targetId(targetMemberId)
                .action(AdminLogAction.MEMBER_UNSUSPEND)
                .reason(reason)
                .build();
    }

    public static AdminLog hidePost(Long actorAdminId, Long targetPostId, String reason) {
        return builder()
                .actorAdminId(actorAdminId)
                .targetId(targetPostId)
                .action(AdminLogAction.POST_HIDE)
                .reason(reason)
                .build();
    }

    public static AdminLog unhidePost(Long actorAdminId, Long targetPostId, String reason) {
        return builder()
                .actorAdminId(actorAdminId)
                .targetId(targetPostId)
                .action(AdminLogAction.POST_UNHIDE)
                .reason(reason)
                .build();
    }

    public static AdminLog hideComment(Long actorAdminId, Long targetCommentId, String reason) {
        return builder()
                .actorAdminId(actorAdminId)
                .targetId(targetCommentId)
                .action(AdminLogAction.COMMENT_HIDE)
                .reason(reason)
                .build();
    }

    public static AdminLog unhideComment(Long actorAdminId, Long targetCommentId, String reason) {
        return builder()
                .actorAdminId(actorAdminId)
                .targetId(targetCommentId)
                .action(AdminLogAction.COMMENT_UNHIDE)
                .reason(reason)
                .build();
    }

    public boolean isFor(AdminLogTargetType targetType, Long targetId) {
        return this.targetType == targetType && this.targetId.equals(targetId);
    }

    private static void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }

    private static String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }

        String trimmedReason = reason.trim();
        return trimmedReason.isEmpty() ? null : trimmedReason;
    }
}
