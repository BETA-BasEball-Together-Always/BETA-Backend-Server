package com.beta.community.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

import com.beta.core.event.notification.PostCommentNotificationEvent;
import com.beta.core.event.notification.PostEmotionNotificationEvent;
import com.beta.core.port.PushPort;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;

@Component
@RequiredArgsConstructor
public class CommunityNotificationEventListener {

    private final UserPort userPort;
    private final PushPort pushPort;
    private final NotificationThrottleService notificationThrottleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostCommentNotificationEvent event) {
        if (!notificationThrottleService.canSendPostComment(
                event.actorUserId(),
                event.targetUserId(),
                event.postId()
        )) {
            return;
        }

        String actorNickname = resolveActorNickname(event.actorUserId());
        pushPort.sendPostCommentNotification(
                event.targetUserId(),
                actorNickname,
                event.postId(),
                event.commentId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostEmotionNotificationEvent event) {
        if (!notificationThrottleService.canSendPostEmotion(
                event.actorUserId(),
                event.targetUserId(),
                event.postId()
        )) {
            return;
        }

        String actorNickname = resolveActorNickname(event.actorUserId());
        pushPort.sendPostEmotionNotification(
                event.targetUserId(),
                actorNickname,
                event.emotionType(),
                event.postId()
        );
    }

    private String resolveActorNickname(Long actorUserId) {
        AuthorInfo actor = userPort.findAuthorsByIds(List.of(actorUserId))
                .getOrDefault(actorUserId, AuthorInfo.unknown(actorUserId));
        return actor.getNickname();
    }
}
