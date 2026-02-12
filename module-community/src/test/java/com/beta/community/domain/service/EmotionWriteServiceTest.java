package com.beta.community.domain.service;

import com.beta.community.domain.entity.Emotion;
import com.beta.community.domain.entity.Emotion.EmotionType;
import com.beta.community.infra.repository.EmotionJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmotionWriteService 단위 테스트")
class EmotionWriteServiceTest {

    @Mock
    private EmotionJpaRepository emotionJpaRepository;

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private EmotionWriteService emotionWriteService;

    @Test
    @DisplayName("감정표현을 저장한다")
    void save() {
        Emotion emotion = Emotion.builder()
                .userId(1L)
                .postId(100L)
                .emotionType(EmotionType.LIKE)
                .build();
        when(emotionJpaRepository.save(any(Emotion.class))).thenReturn(emotion);

        Emotion result = emotionWriteService.save(emotion);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getPostId()).isEqualTo(100L);
        assertThat(result.getEmotionType()).isEqualTo(EmotionType.LIKE);
        verify(emotionJpaRepository).save(emotion);
    }

    @Test
    @DisplayName("감정표현을 삭제한다")
    void delete() {
        Long userId = 1L;
        Long postId = 100L;

        emotionWriteService.delete(userId, postId);

        verify(emotionJpaRepository).deleteByUserIdAndPostId(userId, postId);
    }

    @Test
    @DisplayName("LIKE 카운트를 증가시킨다")
    void incrementLikeCount() {
        Long postId = 100L;

        emotionWriteService.incrementEmotionCount(postId, EmotionType.LIKE);

        verify(postJpaRepository).incrementLikeCount(postId);
    }

    @Test
    @DisplayName("SAD 카운트를 증가시킨다")
    void incrementSadCount() {
        Long postId = 100L;

        emotionWriteService.incrementEmotionCount(postId, EmotionType.SAD);

        verify(postJpaRepository).incrementSadCount(postId);
    }

    @Test
    @DisplayName("FUN 카운트를 증가시킨다")
    void incrementFunCount() {
        Long postId = 100L;

        emotionWriteService.incrementEmotionCount(postId, EmotionType.FUN);

        verify(postJpaRepository).incrementFunCount(postId);
    }

    @Test
    @DisplayName("HYPE 카운트를 증가시킨다")
    void incrementHypeCount() {
        Long postId = 100L;

        emotionWriteService.incrementEmotionCount(postId, EmotionType.HYPE);

        verify(postJpaRepository).incrementHypeCount(postId);
    }

    @Test
    @DisplayName("LIKE 카운트를 감소시킨다")
    void decrementLikeCount() {
        Long postId = 100L;

        emotionWriteService.decrementEmotionCount(postId, EmotionType.LIKE);

        verify(postJpaRepository).decrementLikeCount(postId);
    }

    @Test
    @DisplayName("SAD 카운트를 감소시킨다")
    void decrementSadCount() {
        Long postId = 100L;

        emotionWriteService.decrementEmotionCount(postId, EmotionType.SAD);

        verify(postJpaRepository).decrementSadCount(postId);
    }

    @Test
    @DisplayName("FUN 카운트를 감소시킨다")
    void decrementFunCount() {
        Long postId = 100L;

        emotionWriteService.decrementEmotionCount(postId, EmotionType.FUN);

        verify(postJpaRepository).decrementFunCount(postId);
    }

    @Test
    @DisplayName("HYPE 카운트를 감소시킨다")
    void decrementHypeCount() {
        Long postId = 100L;

        emotionWriteService.decrementEmotionCount(postId, EmotionType.HYPE);

        verify(postJpaRepository).decrementHypeCount(postId);
    }
}
