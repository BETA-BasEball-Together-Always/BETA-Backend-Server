package com.beta.community.application;

import com.beta.core.event.notification.PostCommentNotificationEvent;
import com.beta.core.event.notification.PostEmotionNotificationEvent;
import com.beta.core.port.PushPort;
import com.beta.core.port.UserPort;
import com.beta.core.port.dto.AuthorInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityNotificationEventListener 단위 테스트")
class CommunityNotificationEventListenerTest {

    @Mock
    private UserPort userPort;

    @Mock
    private PushPort pushPort;

    @InjectMocks
    private CommunityNotificationEventListener communityNotificationEventListener;

    @Test
    @DisplayName("댓글 알림 이벤트를 수신하면 게시글 작성자에게 푸시를 보낸다")
    void handle_sendsCommentPush_whenPostCommentNotificationEventReceived() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;
        Long commentId = 100L;

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
    @DisplayName("공감 알림 이벤트를 수신하면 게시글 작성자에게 푸시를 보낸다")
    void handle_sendsEmotionPush_whenPostEmotionNotificationEventReceived() {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;

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
}
