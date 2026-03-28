package com.beta.community.application;

import com.beta.core.event.notification.PostCommentNotificationEvent;
import com.beta.core.event.notification.PostEmotionNotificationEvent;
import com.beta.core.port.PushPort;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@ConditionalOnBean(PushPort.class)
@RequiredArgsConstructor
public class CommunityNotificationEventListener {

    private final UserPort userPort;
    private final PushPort pushPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostCommentNotificationEvent event) {
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
