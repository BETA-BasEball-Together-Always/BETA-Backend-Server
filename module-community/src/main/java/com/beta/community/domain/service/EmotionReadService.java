package com.beta.community.domain.service;

import com.beta.community.domain.entity.Emotion;
import com.beta.community.infra.repository.EmotionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionReadService {

    private final EmotionJpaRepository emotionJpaRepository;

    public Optional<Emotion> findByUserIdAndPostId(Long userId, Long postId) {
        return emotionJpaRepository.findByUserIdAndPostId(userId, postId);
    }
}
