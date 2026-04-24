package com.beta.community.application;

import com.beta.core.event.notification.PostCommentNotificationEvent;
import com.beta.core.event.notification.PostEmotionNotificationEvent;
import com.beta.core.port.PushPort;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityNotificationEventListenerTest {

    @Mock
    private UserPort userPort;

    @Mock
    private PushPort pushPort;

    @Mock
    private NotificationThrottleService notificationThrottleService;

    @InjectMocks
    private CommunityNotificationEventListener communityNotificationEventListener;

    @Test
    void 댓글_알림_이벤트를_수신하고_허용되면_게시글_작성자에게_푸시를_보낸다() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;
        Long commentId = 100L;

        when(notificationThrottleService.canSendPostComment(actorUserId, targetUserId, postId)).thenReturn(true);
        when(userPort.findAuthorsByIds(List.of(actorUserId))).thenReturn(
                Map.of(actorUserId, AuthorInfo.builder()
                        .userId(actorUserId)
                        .nickname("작성자A")
                        .teamCode("LG")
                        .build())
        );

        // when
        communityNotificationEventListener.handle(
                new PostCommentNotificationEvent(actorUserId, targetUserId, postId, commentId)
        );

        // then
        verify(pushPort).sendPostCommentNotification(targetUserId, "작성자A", postId, commentId);
    }

    @Test
    void 댓글_알림_이벤트를_수신하고_차단되면_게시글_작성자에게_푸시를_보내지_않는다() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;
        Long commentId = 100L;

        when(notificationThrottleService.canSendPostComment(actorUserId, targetUserId, postId)).thenReturn(false);

        // when
        communityNotificationEventListener.handle(
                new PostCommentNotificationEvent(actorUserId, targetUserId, postId, commentId)
        );

        // then
        verify(userPort, never()).findAuthorsByIds(List.of(actorUserId));
        verify(pushPort, never()).sendPostCommentNotification(targetUserId, "작성자A", postId, commentId);
    }

    @Test
    void 공감_알림_이벤트를_수신하고_허용되면_게시글_작성자에게_푸시를_보낸다() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;

        when(notificationThrottleService.canSendPostEmotion(actorUserId, targetUserId, postId)).thenReturn(true);
        when(userPort.findAuthorsByIds(List.of(actorUserId))).thenReturn(
                Map.of(actorUserId, AuthorInfo.builder()
                        .userId(actorUserId)
                        .nickname("작성자A")
                        .teamCode("LG")
                        .build())
        );

        // when
        communityNotificationEventListener.handle(
                new PostEmotionNotificationEvent(actorUserId, targetUserId, "LIKE", postId)
        );

        // then
        verify(pushPort).sendPostEmotionNotification(targetUserId, "작성자A", "LIKE", postId);
    }

    @Test
    void 공감_알림_이벤트를_수신하고_차단되면_게시글_작성자에게_푸시를_보내지_않는다() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;

        when(notificationThrottleService.canSendPostEmotion(actorUserId, targetUserId, postId)).thenReturn(false);

        // when
        communityNotificationEventListener.handle(
                new PostEmotionNotificationEvent(actorUserId, targetUserId, "LIKE", postId)
        );

        // then
        verify(userPort, never()).findAuthorsByIds(List.of(actorUserId));
        verify(pushPort, never()).sendPostEmotionNotification(targetUserId, "작성자A", "LIKE", postId);
    }
}
