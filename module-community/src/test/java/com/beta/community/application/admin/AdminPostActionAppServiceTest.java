package com.beta.community.application.admin;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.service.PostReadService;
import com.beta.community.domain.service.PostStatusService;
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
@DisplayName("AdminPostActionAppService 단위 테스트")
class AdminPostActionAppServiceTest {

    @Mock
    PostReadService postReadService;

    @Mock
    PostStatusService postStatusService;

    @Mock
    PostWriteService postWriteService;

    @InjectMocks
    AdminPostActionAppService adminPostActionAppService;

    @Test
    @DisplayName("게시글 숨김 시 조회, 검증, 상태 변경을 순서대로 수행한다")
    void hide_post_calls_read_validate_and_write() {
        // given
        Long postId = 1L;
        Post post = mock(Post.class);
        when(postReadService.findById(postId)).thenReturn(post);

        // when
        adminPostActionAppService.hidePost(postId);

        // then
        verify(postReadService).findById(postId);
        verify(postStatusService).validateHide(post);
        verify(postWriteService).hide(post);
    }

    @Test
    @DisplayName("게시글 다시 노출 시 조회, 검증, 상태 변경을 순서대로 수행한다")
    void unhide_post_calls_read_validate_and_write() {
        // given
        Long postId = 1L;
        Post post = mock(Post.class);
        when(postReadService.findById(postId)).thenReturn(post);

        // when
        adminPostActionAppService.unhidePost(postId);

        // then
        verify(postReadService).findById(postId);
        verify(postStatusService).validateUnhide(post);
        verify(postWriteService).unhide(post);
    }
}
