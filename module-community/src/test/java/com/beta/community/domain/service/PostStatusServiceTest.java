package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.core.exception.admin.InvalidAdminActionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PostStatusService 단위 테스트")
class PostStatusServiceTest {

    final PostStatusService postStatusService = new PostStatusService();

    @Test
    @DisplayName("노출 중인 게시글은 숨김 검증을 통과한다")
    void validate_hide_allows_active_post() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("관리 대상 게시글")
                .channel("ALL")
                .build();
        post.activate();

        // when // then
        assertThatCode(() -> postStatusService.validateHide(post))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제된 게시글은 숨김 처리할 수 없다")
    void validate_hide_rejects_deleted_post() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("관리 대상 게시글")
                .channel("ALL")
                .build();
        post.activate();
        post.softDelete();

        // when // then
        assertThatThrownBy(() -> postStatusService.validateHide(post))
                .isInstanceOf(InvalidAdminActionException.class)
                .hasMessage("삭제된 게시글은 숨김 처리할 수 없습니다.");
    }

    @Test
    @DisplayName("숨김 상태가 아닌 게시글은 다시 노출할 수 없다")
    void validate_unhide_requires_hidden_post() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("관리 대상 게시글")
                .channel("ALL")
                .build();
        post.activate();

        // when // then
        assertThatThrownBy(() -> postStatusService.validateUnhide(post))
                .isInstanceOf(InvalidAdminActionException.class);
    }

    @Test
    @DisplayName("숨김 상태 게시글은 다시 노출할 수 있다")
    void validate_unhide_allows_hidden_post() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("관리 대상 게시글")
                .channel("ALL")
                .build();
        post.activate();
        ReflectionTestUtils.setField(post, "status", Status.HIDDEN);

        // when // then
        assertThatCode(() -> postStatusService.validateUnhide(post))
                .doesNotThrowAnyException();
    }
}
