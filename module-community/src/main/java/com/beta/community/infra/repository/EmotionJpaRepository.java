package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmotionJpaRepository extends JpaRepository<Emotion, Long> {

    Optional<Emotion> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT e.postId FROM Emotion e WHERE e.userId = :userId ORDER BY e.postId DESC")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
