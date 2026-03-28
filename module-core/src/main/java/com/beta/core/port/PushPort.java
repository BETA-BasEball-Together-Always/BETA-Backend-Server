package com.beta.core.port;

public interface PushPort {

    void sendPostCommentNotification(Long targetUserId, String actorNickname, Long postId, Long commentId);

    void sendPostEmotionNotification(Long targetUserId, String actorNickname, String emotionType, Long postId);
}
