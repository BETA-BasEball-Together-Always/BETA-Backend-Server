package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.core.exception.community.PostNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostReadService 단위 테스트")
class PostReadServiceTest {

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private PostReadService postReadService;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 게시글을 조회한다")
        void success() {
            // given
            Post post = Post.builder()
                    .userId(1L)
                    .content("테스트 내용")
                    .channel("ALL")
                    .build();
            when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

            // when
            Post result = postReadService.findById(1L);

            // then
            assertThat(result.getContent()).isEqualTo("테스트 내용");
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 PostNotFoundException 발생")
        void throwException_whenNotFound() {
            // given
            when(postJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postReadService.findById(999L))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findActiveById")
    class FindActiveById {

        @Test
        @DisplayName("활성 상태 게시글을 조회한다")
        void success() {
            // given
            Post post = Post.builder()
                    .userId(1L)
                    .content("테스트 내용")
                    .channel("ALL")
                    .build();
            post.activate();
            when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

            // when
            Post result = postReadService.findActiveById(1L);

            // then
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("삭제된 게시글 조회 시 PostNotFoundException 발생")
        void throwException_whenDeleted() {
            // given
            Post post = Post.builder()
                    .userId(1L)
                    .content("삭제된 게시글")
                    .channel("ALL")
                    .build();
            post.softDelete();
            when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postReadService.findActiveById(1L))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("PENDING 상태 게시글 조회 시 PostNotFoundException 발생")
        void throwException_whenPending() {
            // given
            Post post = Post.builder()
                    .userId(1L)
                    .content("대기 중 게시글")
                    .channel("ALL")
                    .build();
            // status defaults to PENDING
            when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postReadService.findActiveById(1L))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }
}
