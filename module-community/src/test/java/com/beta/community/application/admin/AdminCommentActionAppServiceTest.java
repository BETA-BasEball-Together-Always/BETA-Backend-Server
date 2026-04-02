package com.beta.community.application.admin;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.service.CommentReadService;
import com.beta.community.domain.service.CommentStatusService;
import com.beta.community.domain.service.CommentWriteService;
import com.beta.community.domain.service.PostWriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCommentActionAppService 단위 테스트")
class AdminCommentActionAppServiceTest {

    @Mock
    CommentReadService commentReadService;

    @Mock
    CommentStatusService commentStatusService;

    @Mock
    CommentWriteService commentWriteService;

    @Mock
    PostWriteService postWriteService;

    @InjectMocks
    AdminCommentActionAppService adminCommentActionAppService;

    @Test
    @DisplayName("댓글 숨김 시 조회, 검증, 상태 변경을 순서대로 수행한다")
    void hide_comment_calls_read_validate_and_write() {
        // given
        Long commentId = 1L;
        Comment comment = mock(Comment.class);
        when(commentReadService.findById(commentId)).thenReturn(comment);
        when(comment.getPostId()).thenReturn(101L);

        // when
        adminCommentActionAppService.hideComment(commentId);

        // then
        verify(commentReadService).findById(commentId);
        verify(commentStatusService).validateHide(comment);
        verify(commentWriteService).hide(comment);
        verify(postWriteService).decrementCommentCount(101L);
    }

    @Test
    @DisplayName("댓글 다시 노출 시 조회, 검증, 상태 변경을 순서대로 수행한다")
    void unhide_comment_calls_read_validate_and_write() {
        // given
        Long commentId = 1L;
        Comment comment = mock(Comment.class);
        when(commentReadService.findById(commentId)).thenReturn(comment);
        when(comment.getPostId()).thenReturn(101L);

        // when
        adminCommentActionAppService.unhideComment(commentId);

        // then
        verify(commentReadService).findById(commentId);
        verify(commentStatusService).validateUnhide(comment);
        verify(commentWriteService).unhide(comment);
        verify(postWriteService).incrementCommentCount(101L);
    }
}
