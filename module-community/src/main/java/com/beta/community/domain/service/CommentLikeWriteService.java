package com.beta.community.domain.service;

import com.beta.community.domain.entity.CommentLike;
import com.beta.community.infra.repository.CommentLikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentLikeWriteService {

    private final CommentLikeJpaRepository commentLikeJpaRepository;

    public void save(CommentLike commentLike) {
        commentLikeJpaRepository.save(commentLike);
    }

    public void delete(Long userId, Long commentId) {
        commentLikeJpaRepository.deleteByUserIdAndCommentId(userId, commentId);
    }
}
