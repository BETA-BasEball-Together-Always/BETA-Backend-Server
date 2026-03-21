package com.beta.community.application.admin.dto;

import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.AdminCommentQueryRepository;

import java.time.LocalDateTime;

public record AdminCommentQueryResult(
        Long commentId,
        Long authorUserId,
        String authorNickname,
        Long postId,
        String content,
        Integer depth,
        Status status,
        LocalDateTime createdAt
) {
    public static AdminCommentQueryResult from(AdminCommentQueryRepository.CommentSummarySnapshot snapshot) {
        return new AdminCommentQueryResult(
                snapshot.commentId(),
                snapshot.authorUserId(),
                null,
                snapshot.postId(),
                snapshot.content(),
                snapshot.depth(),
                snapshot.status(),
                snapshot.createdAt()
        );
    }

    public AdminCommentQueryResult withAuthorNickname(String authorNickname) {
        return new AdminCommentQueryResult(
                commentId,
                authorUserId,
                authorNickname,
                postId,
                content,
                depth,
                status,
                createdAt
        );
    }
}
