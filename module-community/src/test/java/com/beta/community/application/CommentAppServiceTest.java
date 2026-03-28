package com.beta.community.application;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.service.CommentLikeReadService;
import com.beta.community.domain.service.CommentLikeWriteService;
import com.beta.community.domain.service.CommentReadService;
import com.beta.community.domain.service.CommentWriteService;
import com.beta.community.domain.service.IdempotencyService;
import com.beta.community.domain.service.PostReadService;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.core.event.notification.PostCommentNotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentAppService 단위 테스트")
class CommentAppServiceTest {

    @Mock
    private PostReadService postReadService;

    @Mock
    private CommentReadService commentReadService;

    @Mock
    private CommentWriteService commentWriteService;

    @Mock
    private CommentLikeReadService commentLikeReadService;

    @Mock
    private CommentLikeWriteService commentLikeWriteService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private PostJpaRepository postJpaRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CommentAppService commentAppService;

    @Test
    @DisplayName("다른 사용자의 게시글에 댓글 작성 시 게시글 작성자에게 푸시를 보낸다")
    void createComment_sendsPushToPostOwner_whenCommentingOthersPost() throws Exception {
        // given
        Long actorUserId = 1L;
        Long targetUserId = 2L;
        Long postId = 10L;
        Long commentId = 100L;

        Post post = Post.builder()
                .userId(targetUserId)
                .content("post")
                .channel("ALL")
                .build();
        post.activate();

        Comment savedComment = Comment.builder()
                .postId(postId)
                .userId(actorUserId)
                .content("댓글")
                .depth(0)
                .build();
        setId(savedComment, commentId);

        when(postReadService.findActiveById(postId)).thenReturn(post);
        when(idempotencyService.isDuplicateComment(actorUserId, postId, "댓글")).thenReturn(false);
        when(commentWriteService.save(any(Comment.class))).thenReturn(savedComment);
        // when
        commentAppService.createComment(actorUserId, postId, "댓글", null);

        // then
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        PostCommentNotificationEvent event =
                assertInstanceOf(PostCommentNotificationEvent.class, eventCaptor.getValue());
        assertEquals(actorUserId, event.actorUserId());
        assertEquals(targetUserId, event.targetUserId());
        assertEquals(postId, event.postId());
        assertEquals(commentId, event.commentId());
    }

    @Test
    @DisplayName("자신의 게시글에 댓글 작성 시 푸시를 보내지 않는다")
    void createComment_doesNotSendPush_whenCommentingOwnPost() {
        // given
        Long actorUserId = 1L;
        Long postId = 10L;

        Post post = Post.builder()
                .userId(actorUserId)
                .content("post")
                .channel("ALL")
                .build();
        post.activate();

        Comment savedComment = Comment.builder()
                .postId(postId)
                .userId(actorUserId)
                .content("댓글")
                .depth(0)
                .build();

        when(postReadService.findActiveById(postId)).thenReturn(post);
        when(idempotencyService.isDuplicateComment(actorUserId, postId, "댓글")).thenReturn(false);
        when(commentWriteService.save(any(Comment.class))).thenReturn(savedComment);

        // when
        commentAppService.createComment(actorUserId, postId, "댓글", null);

        // then
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    private void setId(Comment comment, Long id) throws Exception {
        Field idField = comment.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(comment, id);
    }
}
