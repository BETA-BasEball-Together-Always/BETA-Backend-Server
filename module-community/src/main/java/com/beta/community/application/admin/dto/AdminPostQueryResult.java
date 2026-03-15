package com.beta.community.application.admin.dto;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.AdminPostQueryRepository;

import java.time.LocalDateTime;

public record AdminPostQueryResult(
        Long postId,
        Long authorUserId,
        String authorNickname,
        String content,
        Post.Channel channel,
        Status status,
        LocalDateTime createdAt
) {
    public static AdminPostQueryResult from(AdminPostQueryRepository.PostSummarySnapshot snapshot) {
        return new AdminPostQueryResult(
                snapshot.postId(),
                snapshot.authorUserId(),
                null,
                snapshot.content(),
                snapshot.channel(),
                snapshot.status(),
                snapshot.createdAt()
        );
    }

    public AdminPostQueryResult withAuthorNickname(String authorNickname) {
        return new AdminPostQueryResult(
                postId,
                authorUserId,
                authorNickname,
                content,
                channel,
                status,
                createdAt
        );
    }
}
