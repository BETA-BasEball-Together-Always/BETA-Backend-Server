package com.beta.core.event.notification;

public record PostCommentNotificationEvent(
        Long actorUserId,
        Long targetUserId,
        Long postId,
        Long commentId
) {
}
