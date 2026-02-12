package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionJpaRepository extends JpaRepository<Emotion, Long> {

    Optional<Emotion> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);
}
