package com.beta.community.domain.service;

import com.beta.community.domain.entity.CommentLike;
import com.beta.community.infra.repository.CommentLikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeReadService {

    private final CommentLikeJpaRepository commentLikeJpaRepository;

    public Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId) {
        return commentLikeJpaRepository.findByUserIdAndCommentId(userId, commentId);
    }

    public boolean existsByUserIdAndCommentId(Long userId, Long commentId) {
        return commentLikeJpaRepository.existsByUserIdAndCommentId(userId, commentId);
    }
}
