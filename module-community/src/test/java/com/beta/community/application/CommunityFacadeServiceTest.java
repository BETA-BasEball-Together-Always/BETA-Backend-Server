package com.beta.community.application;

import com.beta.community.domain.entity.Emotion;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.service.ChannelValidationService;
import com.beta.community.domain.service.CommentReadService;
import com.beta.community.domain.service.EmotionReadService;
import com.beta.community.domain.service.EmotionWriteService;
import com.beta.community.domain.service.HashtagService;
import com.beta.community.domain.service.IdempotencyService;
import com.beta.community.domain.service.PostImageService;
import com.beta.community.domain.service.PostReadService;
import com.beta.community.domain.service.PostWriteService;
import com.beta.community.domain.service.UserBlockReadService;
import com.beta.community.domain.service.UserBlockWriteService;
import com.beta.community.infra.repository.CommentLikeJpaRepository;
import com.beta.community.infra.repository.PostHashtagJpaRepository;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.community.infra.repository.PostQueryRepository;
import com.beta.core.event.notification.PostEmotionNotificationEvent;
import com.beta.core.port.UserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityFacadeService 단위 테스트")
class CommunityFacadeServiceTest {

    @Mock private PostReadService postReadService;
    @Mock private PostWriteService postWriteService;
    @Mock private PostImageService postImageService;
    @Mock private HashtagService hashtagService;
    @Mock private ChannelValidationService channelValidationService;
    @Mock private IdempotencyService idempotencyService;
    @Mock private UserBlockReadService userBlockReadService;
    @Mock private UserBlockWriteService userBlockWriteService;
    @Mock private CommentReadService commentReadService;
    @Mock private EmotionReadService emotionReadService;
    @Mock private EmotionWriteService emotionWriteService;
    @Mock private PostQueryRepository postQueryRepository;
    @Mock private PostJpaRepository postJpaRepository;
    @Mock private PostImageJpaRepository postImageJpaRepository;
    @Mock private PostHashtagJpaRepository postHashtagJpaRepository;
    @Mock private CommentLikeJpaRepository commentLikeJpaRepository;
    @Mock private UserPort userPort;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CommunityFacadeService communityFacadeService;

    @Test
    @DisplayName("다른 사용자의 게시글에 공감 추가 시 게시글 작성자에게 푸시를 보낸다")
    void toggleEmotion_sendsPush_whenReactingOthersPost() throws Exception {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;

        Post post = Post.builder()
                .userId(targetUserId)
                .content("post")
                .channel("ALL")
                .build();
        post.activate();
        setId(post, postId);

        when(postReadService.findActiveById(postId)).thenReturn(post);
        when(emotionReadService.findByUserIdAndPostId(actorUserId, postId)).thenReturn(Optional.empty());
        // when
        communityFacadeService.toggleEmotion(actorUserId, postId, "LIKE");

        // then
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        PostEmotionNotificationEvent event =
                assertInstanceOf(PostEmotionNotificationEvent.class, eventCaptor.getValue());
        assertEquals(actorUserId, event.actorUserId());
        assertEquals(targetUserId, event.targetUserId());
        assertEquals(postId, event.postId());
        assertEquals("LIKE", event.emotionType());
    }

    @Test
    @DisplayName("같은 공감을 다시 눌러 취소한 경우 푸시를 보내지 않는다")
    void toggleEmotion_doesNotSendPush_whenEmotionRemoved() throws Exception {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;

        Post post = Post.builder()
                .userId(targetUserId)
                .content("post")
                .channel("ALL")
                .build();
        post.activate();
        setId(post, postId);

        Emotion existingEmotion = Emotion.builder()
                .userId(actorUserId)
                .postId(postId)
                .emotionType(Emotion.EmotionType.LIKE)
                .build();

        when(postReadService.findActiveById(postId)).thenReturn(post);
        when(emotionReadService.findByUserIdAndPostId(actorUserId, postId)).thenReturn(Optional.of(existingEmotion));

        // when
        communityFacadeService.toggleEmotion(actorUserId, postId, "LIKE");

        // then
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    private void setId(Post post, Long id) throws Exception {
        Field idField = post.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(post, id);
    }
}
