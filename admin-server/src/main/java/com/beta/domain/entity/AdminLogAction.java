package com.beta.domain.entity;

public enum AdminLogAction {
    MEMBER_SUSPEND(AdminLogTargetType.MEMBER, "ACTIVE", "SUSPENDED"),
    MEMBER_UNSUSPEND(AdminLogTargetType.MEMBER, "SUSPENDED", "ACTIVE"),
    POST_HIDE(AdminLogTargetType.POST, "ACTIVE", "HIDDEN"),
    POST_UNHIDE(AdminLogTargetType.POST, "HIDDEN", "ACTIVE"),
    COMMENT_HIDE(AdminLogTargetType.COMMENT, "ACTIVE", "HIDDEN"),
    COMMENT_UNHIDE(AdminLogTargetType.COMMENT, "HIDDEN", "ACTIVE");

    private final AdminLogTargetType targetType;
    private final String beforeStatus;
    private final String afterStatus;

    AdminLogAction(AdminLogTargetType targetType, String beforeStatus, String afterStatus) {
        this.targetType = targetType;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
    }

    public AdminLogTargetType getTargetType() {
        return targetType;
    }

    public String getBeforeStatus() {
        return beforeStatus;
    }

    public String getAfterStatus() {
        return afterStatus;
    }
}
