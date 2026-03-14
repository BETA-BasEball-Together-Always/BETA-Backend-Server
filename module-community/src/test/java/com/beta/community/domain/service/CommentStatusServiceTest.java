package com.beta.community.domain.service;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import com.beta.core.exception.admin.InvalidAdminActionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommentStatusService 단위 테스트")
class CommentStatusServiceTest {

    final CommentStatusService commentStatusService = new CommentStatusService();

    @Test
    @DisplayName("노출 중인 댓글은 숨김 검증을 통과한다")
    void validate_hide_allows_active_comment() {
        // given
        Comment comment = Comment.builder()
                .postId(1L)
                .userId(1L)
                .content("관리 대상 댓글")
                .build();

        // when // then
        assertThatCode(() -> commentStatusService.validateHide(comment))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제된 댓글은 숨김 처리할 수 없다")
    void validate_hide_rejects_deleted_comment() {
        // given
        Comment comment = Comment.builder()
                .postId(1L)
                .userId(1L)
                .content("관리 대상 댓글")
                .build();
        comment.softDelete();

        // when // then
        assertThatThrownBy(() -> commentStatusService.validateHide(comment))
                .isInstanceOf(InvalidAdminActionException.class)
                .hasMessage("삭제된 댓글은 숨김 처리할 수 없습니다.");
    }

    @Test
    @DisplayName("숨김 상태가 아닌 댓글은 다시 노출할 수 없다")
    void validate_unhide_requires_hidden_comment() {
        // given
        Comment comment = Comment.builder()
                .postId(1L)
                .userId(1L)
                .content("관리 대상 댓글")
                .build();

        // when // then
        assertThatThrownBy(() -> commentStatusService.validateUnhide(comment))
                .isInstanceOf(InvalidAdminActionException.class);
    }

    @Test
    @DisplayName("숨김 상태 댓글은 다시 노출할 수 있다")
    void validate_unhide_allows_hidden_comment() {
        // given
        Comment comment = Comment.builder()
                .postId(1L)
                .userId(1L)
                .content("관리 대상 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "status", Status.HIDDEN);

        // when // then
        assertThatCode(() -> commentStatusService.validateUnhide(comment))
                .doesNotThrowAnyException();
    }
}
