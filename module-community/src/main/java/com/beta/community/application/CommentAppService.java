package com.beta.community.application;

import com.beta.community.application.dto.CommentCreateDto;
import com.beta.community.application.dto.CommentLikeToggleDto;
import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.CommentLike;
import com.beta.community.domain.service.CommentLikeReadService;
import com.beta.community.domain.service.CommentLikeWriteService;
import com.beta.community.domain.service.CommentReadService;
import com.beta.community.domain.service.CommentWriteService;
import com.beta.community.domain.service.IdempotencyService;
import com.beta.community.domain.service.PostReadService;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.core.exception.community.CommentAccessDeniedException;
import com.beta.core.exception.community.CommentDepthExceededException;
import com.beta.core.exception.community.DuplicateCommentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAppService {

    private final PostReadService postReadService;
    private final CommentReadService commentReadService;
    private final CommentWriteService commentWriteService;
    private final CommentLikeReadService commentLikeReadService;
    private final CommentLikeWriteService commentLikeWriteService;
    private final IdempotencyService idempotencyService;
    private final PostJpaRepository postJpaRepository;

    @Transactional
    public CommentCreateDto createComment(Long userId, Long postId, String content, Long parentId) {
        postReadService.findActiveById(postId);

        if (idempotencyService.isDuplicateComment(userId, postId, content)) {
            throw new DuplicateCommentException();
        }

        int depth = 0;
        if (parentId != null) {
            Comment parentComment = commentReadService.findActiveById(parentId);
            if (parentComment.getDepth() >= 1) {
                throw new CommentDepthExceededException();
            }
            depth = 1;
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .parentId(parentId)
                .depth(depth)
                .build();

        Comment savedComment = commentWriteService.save(comment);
        postJpaRepository.incrementCommentCount(postId);

        return CommentCreateDto.builder()
                .commentId(savedComment.getId())
                .postId(savedComment.getPostId())
                .userId(savedComment.getUserId())
                .content(savedComment.getContent())
                .parentId(savedComment.getParentId())
                .depth(savedComment.getDepth())
                .createdAt(savedComment.getCreatedAt())
                .build();
    }

    @Transactional
    public void updateComment(Long userId, Long commentId, String content) {
        Comment comment = commentReadService.findActiveById(commentId);

        if (!comment.isOwnedBy(userId)) {
            throw new CommentAccessDeniedException();
        }

        comment.updateContent(content);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentReadService.findActiveById(commentId);

        if (!comment.isOwnedBy(userId)) {
            throw new CommentAccessDeniedException();
        }

        commentWriteService.softDelete(comment);
        postJpaRepository.decrementCommentCount(comment.getPostId());
    }

    @Transactional
    public CommentLikeToggleDto toggleCommentLike(Long userId, Long commentId) {
        commentReadService.findActiveById(commentId);

        boolean liked;
        if (commentLikeReadService.existsByUserIdAndCommentId(userId, commentId)) {
            commentLikeWriteService.delete(userId, commentId);
            commentWriteService.decrementLikeCount(commentId);
            liked = false;
        } else {
            CommentLike commentLike = CommentLike.builder()
                    .userId(userId)
                    .commentId(commentId)
                    .build();
            commentLikeWriteService.save(commentLike);
            commentWriteService.incrementLikeCount(commentId);
            liked = true;
        }

        Comment updatedComment = commentReadService.findById(commentId);
        return CommentLikeToggleDto.builder()
                .commentId(commentId)
                .liked(liked)
                .likeCount(updatedComment.getLikeCount())
                .build();
    }
}
