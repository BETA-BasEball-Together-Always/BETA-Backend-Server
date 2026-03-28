package com.beta.core.event.notification;

public record PostEmotionNotificationEvent(
        Long actorUserId,
        Long targetUserId,
        String emotionType,
        Long postId
) {
}
