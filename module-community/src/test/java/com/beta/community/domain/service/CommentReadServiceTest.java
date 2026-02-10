package com.beta.community.domain.service;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.CommentJpaRepository;
import com.beta.core.exception.community.CommentNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentReadService 단위 테스트")
class CommentReadServiceTest {

    @Mock
    private CommentJpaRepository commentJpaRepository;

    @InjectMocks
    private CommentReadService commentReadService;

    @Nested
    @DisplayName("findParentComments")
    class FindParentComments {

        @Test
        @DisplayName("부모 댓글을 커서 기반으로 조회한다")
        void success() {
            // given
            Long postId = 1L;
            Long cursor = 0L;
            int size = 20;
            List<Comment> comments = List.of(
                    createComment(1L, postId, null, 0),
                    createComment(2L, postId, null, 0)
            );
            when(commentJpaRepository.findParentComments(eq(postId), eq(cursor), any(PageRequest.class)))
                    .thenReturn(comments);

            // when
            List<Comment> result = commentReadService.findParentComments(postId, cursor, size);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("커서가 null이면 0으로 대체된다")
        void cursorNullToZero() {
            // given
            Long postId = 1L;
            int size = 20;
            when(commentJpaRepository.findParentComments(eq(postId), eq(0L), any(PageRequest.class)))
                    .thenReturn(List.of());

            // when
            List<Comment> result = commentReadService.findParentComments(postId, null, size);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRepliesByParentIds")
    class FindRepliesByParentIds {

        @Test
        @DisplayName("부모 ID 목록으로 대댓글을 조회한다")
        void success() {
            // given
            Long postId = 1L;
            List<Long> parentIds = List.of(1L, 2L);
            List<Comment> replies = List.of(
                    createComment(10L, postId, 1L, 1),
                    createComment(11L, postId, 2L, 1)
            );
            when(commentJpaRepository.findRepliesByParentIds(postId, parentIds))
                    .thenReturn(replies);

            // when
            List<Comment> result = commentReadService.findRepliesByParentIds(postId, parentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getParentId()).isEqualTo(1L);
            assertThat(result.get(1).getParentId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("부모 ID 목록이 비어있으면 빈 리스트를 반환한다")
        void emptyParentIds() {
            // given
            Long postId = 1L;
            List<Long> parentIds = List.of();

            // when
            List<Comment> result = commentReadService.findRepliesByParentIds(postId, parentIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findParentIdsWithActiveReplies")
    class FindParentIdsWithActiveReplies {

        @Test
        @DisplayName("활성 답글이 있는 부모 ID를 반환한다")
        void success() {
            // given
            List<Long> parentIds = List.of(1L, 2L, 3L);
            when(commentJpaRepository.findParentIdsWithActiveReplies(parentIds, Status.ACTIVE))
                    .thenReturn(List.of(1L, 3L));

            // when
            Set<Long> result = commentReadService.findParentIdsWithActiveReplies(parentIds);

            // then
            assertThat(result).containsExactlyInAnyOrder(1L, 3L);
        }

        @Test
        @DisplayName("부모 ID 목록이 비어있으면 빈 Set을 반환한다")
        void emptyParentIds() {
            // given
            List<Long> parentIds = List.of();

            // when
            Set<Long> result = commentReadService.findParentIdsWithActiveReplies(parentIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("댓글을 ID로 조회한다")
        void success() {
            // given
            Long commentId = 1L;
            Comment comment = createComment(commentId, 1L, null, 0);
            when(commentJpaRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when
            Comment result = commentReadService.findById(commentId);

            // then
            assertThat(result.getId()).isEqualTo(commentId);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 조회 시 CommentNotFoundException 발생")
        void notFound() {
            // given
            Long commentId = 999L;
            when(commentJpaRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentReadService.findById(commentId))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findActiveById")
    class FindActiveById {

        @Test
        @DisplayName("활성 댓글을 조회한다")
        void success() {
            // given
            Long commentId = 1L;
            Comment comment = createComment(commentId, 1L, null, 0);
            when(commentJpaRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when
            Comment result = commentReadService.findActiveById(commentId);

            // then
            assertThat(result.getId()).isEqualTo(commentId);
            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("삭제된 댓글 조회 시 CommentNotFoundException 발생")
        void deletedComment() {
            // given
            Long commentId = 1L;
            Comment comment = createComment(commentId, 1L, null, 0);
            comment.softDelete();
            when(commentJpaRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentReadService.findActiveById(commentId))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 조회 시 CommentNotFoundException 발생")
        void notFound() {
            // given
            Long commentId = 999L;
            when(commentJpaRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentReadService.findActiveById(commentId))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }

    private Comment createComment(Long id, Long postId, Long parentId, Integer depth) {
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(1L)
                .content("테스트 댓글")
                .parentId(parentId)
                .depth(depth)
                .build();
        // Reflection으로 ID 설정 (테스트 목적)
        try {
            var idField = comment.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(comment, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return comment;
    }
}
