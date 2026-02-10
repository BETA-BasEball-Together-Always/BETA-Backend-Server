package com.beta.community.domain.service;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.CommentJpaRepository;
import com.beta.core.exception.community.CommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentReadService {

    private final CommentJpaRepository commentJpaRepository;

    public List<Comment> findParentComments(Long postId, Long cursor, int size) {
        Long effectiveCursor = (cursor != null) ? cursor : 0L;
        return commentJpaRepository.findParentComments(postId, effectiveCursor, PageRequest.of(0, size));
    }

    public List<Comment> findRepliesByParentIds(Long postId, List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return List.of();
        }
        return commentJpaRepository.findRepliesByParentIds(postId, parentIds);
    }

    public Set<Long> findParentIdsWithActiveReplies(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Set.of();
        }
        List<Long> result = commentJpaRepository.findParentIdsWithActiveReplies(parentIds, Status.ACTIVE);
        return Set.copyOf(result);
    }

    public Comment findById(Long commentId) {
        return commentJpaRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }

    public Comment findActiveById(Long commentId) {
        Comment comment = findById(commentId);
        if (!comment.isActive()) {
            throw new CommentNotFoundException();
        }
        return comment;
    }
}
