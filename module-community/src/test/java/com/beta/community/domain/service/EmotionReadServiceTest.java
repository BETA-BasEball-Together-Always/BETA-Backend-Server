package com.beta.community.domain.service;

import com.beta.community.domain.entity.Emotion;
import com.beta.community.domain.entity.Emotion.EmotionType;
import com.beta.community.infra.repository.EmotionJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmotionReadService 단위 테스트")
class EmotionReadServiceTest {

    @Mock
    private EmotionJpaRepository emotionJpaRepository;

    @InjectMocks
    private EmotionReadService emotionReadService;

    @Test
    @DisplayName("userId와 postId로 감정표현을 조회한다")
    void findByUserIdAndPostId() {
        Long userId = 1L;
        Long postId = 100L;
        Emotion emotion = Emotion.builder()
                .userId(userId)
                .postId(postId)
                .emotionType(EmotionType.LIKE)
                .build();
        when(emotionJpaRepository.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.of(emotion));

        Optional<Emotion> result = emotionReadService.findByUserIdAndPostId(userId, postId);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getPostId()).isEqualTo(postId);
        assertThat(result.get().getEmotionType()).isEqualTo(EmotionType.LIKE);
        verify(emotionJpaRepository).findByUserIdAndPostId(userId, postId);
    }

    @Test
    @DisplayName("감정표현이 없으면 빈 Optional을 반환한다")
    void findByUserIdAndPostId_NotFound() {
        Long userId = 1L;
        Long postId = 100L;
        when(emotionJpaRepository.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.empty());

        Optional<Emotion> result = emotionReadService.findByUserIdAndPostId(userId, postId);

        assertThat(result).isEmpty();
        verify(emotionJpaRepository).findByUserIdAndPostId(userId, postId);
    }
}
