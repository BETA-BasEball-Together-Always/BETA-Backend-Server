package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostWriteService 단위 테스트")
class PostWriteServiceTest {

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private PostWriteService postWriteService;

    @Test
    @DisplayName("게시글을 저장한다")
    void save() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("테스트 내용")
                .channel("ALL")
                .build();
        when(postJpaRepository.save(any(Post.class))).thenReturn(post);

        // when
        Post result = postWriteService.save(post);

        // then
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        verify(postJpaRepository).save(post);
    }

    @Test
    @DisplayName("게시글 내용을 수정한다")
    void updateContent() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("원래 내용")
                .channel("ALL")
                .build();

        // when
        postWriteService.updateContent(post, "수정된 내용");

        // then
        assertThat(post.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글을 소프트 삭제한다")
    void softDelete() {
        // given
        Post post = Post.builder()
                .userId(1L)
                .content("소프트 삭제할 내용")
                .channel("ALL")
                .build();
        post.activate();

        // when
        postWriteService.softDelete(post);

        // then
        assertThat(post.getStatus()).isEqualTo(Status.DELETED);
        assertThat(post.getDeletedAt()).isNotNull();
    }
}
