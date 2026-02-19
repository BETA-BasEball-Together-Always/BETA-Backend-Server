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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CommentLikeWriteServiceTest {

    @InjectMocks
    private CommentLikeWriteService commentLikeWriteService;

    @Mock
    private CommentLikeJpaRepository commentLikeJpaRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("댓글 좋아요를 저장한다")
        void saveCommentLike() {
            // given
            CommentLike commentLike = CommentLike.builder()
                    .userId(1L)
                    .commentId(1L)
                    .build();

            // when
            commentLikeWriteService.save(commentLike);

            // then
            then(commentLikeJpaRepository).should().save(any(CommentLike.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("사용자와 댓글 ID로 좋아요를 삭제한다")
        void deleteCommentLike() {
            // given
            Long userId = 1L;
            Long commentId = 1L;

            // when
            commentLikeWriteService.delete(userId, commentId);

            // then
            then(commentLikeJpaRepository).should().deleteByUserIdAndCommentId(userId, commentId);
        }
    }
}
