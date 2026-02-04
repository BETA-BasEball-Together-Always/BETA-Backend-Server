package com.beta.community.domain.service;

import com.beta.community.infra.repository.IdempotencyRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyService 단위 테스트")
class IdempotencyServiceTest {

    @Mock
    private IdempotencyRedisRepository idempotencyRedisRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    @DisplayName("동일한 게시글 내용으로 30초 이내 재요청 시 중복으로 판단한다")
    void isDuplicatePost_returnTrue_whenDuplicate() {
        // given
        when(idempotencyRedisRepository.setIfAbsent(anyString())).thenReturn(false);

        // when
        boolean result = idempotencyService.isDuplicatePost(1L, "테스트 내용");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("새로운 게시글 내용은 중복이 아니다")
    void isDuplicatePost_returnFalse_whenNotDuplicate() {
        // given
        when(idempotencyRedisRepository.setIfAbsent(anyString())).thenReturn(true);

        // when
        boolean result = idempotencyService.isDuplicatePost(1L, "새로운 내용");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("동일한 댓글 내용으로 30초 이내 재요청 시 중복으로 판단한다")
    void isDuplicateComment_returnTrue_whenDuplicate() {
        // given
        when(idempotencyRedisRepository.setIfAbsent(anyString())).thenReturn(false);

        // when
        boolean result = idempotencyService.isDuplicateComment(1L, 100L, "댓글 내용");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("새로운 댓글 내용은 중복이 아니다")
    void isDuplicateComment_returnFalse_whenNotDuplicate() {
        // given
        when(idempotencyRedisRepository.setIfAbsent(anyString())).thenReturn(true);

        // when
        boolean result = idempotencyService.isDuplicateComment(1L, 100L, "새 댓글");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 사용자의 동일 내용은 중복이 아니다")
    void isDuplicatePost_differentUser_notDuplicate() {
        // given - 첫 번째 사용자 성공
        when(idempotencyRedisRepository.setIfAbsent(anyString())).thenReturn(true);

        // when
        boolean user1Result = idempotencyService.isDuplicatePost(1L, "동일 내용");
        boolean user2Result = idempotencyService.isDuplicatePost(2L, "동일 내용");

        // then
        assertThat(user1Result).isFalse();
        assertThat(user2Result).isFalse();
    }
}
