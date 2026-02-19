package com.beta.community.domain.service;

import com.beta.community.domain.entity.CommentLike;
import com.beta.community.infra.repository.CommentLikeJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentLikeReadServiceTest {

    @InjectMocks
    private CommentLikeReadService commentLikeReadService;

    @Mock
    private CommentLikeJpaRepository commentLikeJpaRepository;

    @Nested
    @DisplayName("findByUserIdAndCommentId")
    class FindByUserIdAndCommentId {

        @Test
        @DisplayName("사용자와 댓글 ID로 좋아요를 조회한다")
        void findByUserIdAndCommentId() {
            // given
            Long userId = 1L;
            Long commentId = 1L;
            CommentLike commentLike = CommentLike.builder()
                    .userId(userId)
                    .commentId(commentId)
                    .build();

            given(commentLikeJpaRepository.findByUserIdAndCommentId(userId, commentId))
                    .willReturn(Optional.of(commentLike));

            // when
            Optional<CommentLike> result = commentLikeReadService.findByUserIdAndCommentId(userId, commentId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(userId);
            assertThat(result.get().getCommentId()).isEqualTo(commentId);
        }

        @Test
        @DisplayName("좋아요가 없으면 빈 Optional을 반환한다")
        void returnEmptyWhenNotFound() {
            // given
            Long userId = 1L;
            Long commentId = 1L;

            given(commentLikeJpaRepository.findByUserIdAndCommentId(userId, commentId))
                    .willReturn(Optional.empty());

            // when
            Optional<CommentLike> result = commentLikeReadService.findByUserIdAndCommentId(userId, commentId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndCommentId")
    class ExistsByUserIdAndCommentId {

        @Test
        @DisplayName("좋아요가 존재하면 true를 반환한다")
        void returnTrueWhenExists() {
            // given
            Long userId = 1L;
            Long commentId = 1L;

            given(commentLikeJpaRepository.existsByUserIdAndCommentId(userId, commentId))
                    .willReturn(true);

            // when
            boolean result = commentLikeReadService.existsByUserIdAndCommentId(userId, commentId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("좋아요가 없으면 false를 반환한다")
        void returnFalseWhenNotExists() {
            // given
            Long userId = 1L;
            Long commentId = 1L;

            given(commentLikeJpaRepository.existsByUserIdAndCommentId(userId, commentId))
                    .willReturn(false);

            // when
            boolean result = commentLikeReadService.existsByUserIdAndCommentId(userId, commentId);

            // then
            assertThat(result).isFalse();
        }
    }
}
