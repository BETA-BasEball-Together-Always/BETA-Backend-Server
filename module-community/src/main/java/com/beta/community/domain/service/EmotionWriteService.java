package com.beta.community.domain.service;

import com.beta.community.domain.entity.Emotion;
import com.beta.community.domain.entity.Emotion.EmotionType;
import com.beta.community.infra.repository.EmotionJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmotionWriteService {

    private final EmotionJpaRepository emotionJpaRepository;
    private final PostJpaRepository postJpaRepository;

    public Emotion save(Emotion emotion) {
        return emotionJpaRepository.save(emotion);
    }

    public void delete(Long userId, Long postId) {
        emotionJpaRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public void incrementEmotionCount(Long postId, EmotionType emotionType) {
        switch (emotionType) {
            case LIKE -> postJpaRepository.incrementLikeCount(postId);
            case SAD -> postJpaRepository.incrementSadCount(postId);
            case FUN -> postJpaRepository.incrementFunCount(postId);
            case HYPE -> postJpaRepository.incrementHypeCount(postId);
        }
    }

    public void decrementEmotionCount(Long postId, EmotionType emotionType) {
        switch (emotionType) {
            case LIKE -> postJpaRepository.decrementLikeCount(postId);
            case SAD -> postJpaRepository.decrementSadCount(postId);
            case FUN -> postJpaRepository.decrementFunCount(postId);
            case HYPE -> postJpaRepository.decrementHypeCount(postId);
        }
    }
}
