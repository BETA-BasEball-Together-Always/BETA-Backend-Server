package com.beta.community.domain.service;

import com.beta.community.domain.entity.Comment;
import com.beta.community.infra.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentWriteService {

    private final CommentJpaRepository commentJpaRepository;

    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    public void softDelete(Comment comment) {
        comment.softDelete();
    }

    public void incrementLikeCount(Long commentId) {
        commentJpaRepository.incrementLikeCount(commentId);
    }

    public void decrementLikeCount(Long commentId) {
        commentJpaRepository.decrementLikeCount(commentId);
    }
}
