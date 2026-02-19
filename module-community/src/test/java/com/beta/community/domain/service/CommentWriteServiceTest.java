package com.beta.community.domain.service;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.CommentJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CommentWriteServiceTest {

    @InjectMocks
    private CommentWriteService commentWriteService;

    @Mock
    private CommentJpaRepository commentJpaRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("댓글을 저장하고 반환한다")
        void saveComment() {
            // given
            Comment comment = Comment.builder()
                    .postId(1L)
                    .userId(1L)
                    .content("테스트 댓글")
                    .depth(0)
                    .build();

            given(commentJpaRepository.save(any(Comment.class))).willReturn(comment);

            // when
            Comment result = commentWriteService.save(comment);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("테스트 댓글");
            then(commentJpaRepository).should().save(comment);
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("댓글을 soft delete 처리한다")
        void softDeleteComment() {
            // given
            Comment comment = Comment.builder()
                    .postId(1L)
                    .userId(1L)
                    .content("테스트 댓글")
                    .depth(0)
                    .build();

            // when
            commentWriteService.softDelete(comment);

            // then
            assertThat(comment.getStatus()).isEqualTo(Status.DELETED);
            assertThat(comment.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("incrementLikeCount")
    class IncrementLikeCount {

        @Test
        @DisplayName("댓글 좋아요 수를 증가시킨다")
        void incrementLikeCount() {
            // given
            Long commentId = 1L;

            // when
            commentWriteService.incrementLikeCount(commentId);

            // then
            then(commentJpaRepository).should().incrementLikeCount(commentId);
        }
    }

    @Nested
    @DisplayName("decrementLikeCount")
    class DecrementLikeCount {

        @Test
        @DisplayName("댓글 좋아요 수를 감소시킨다")
        void decrementLikeCount() {
            // given
            Long commentId = 1L;

            // when
            commentWriteService.decrementLikeCount(commentId);

            // then
            then(commentJpaRepository).should().decrementLikeCount(commentId);
        }
    }
}
