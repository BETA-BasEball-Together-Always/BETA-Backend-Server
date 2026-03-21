package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.QPost;
import com.beta.community.domain.entity.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminPostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<PostSummarySnapshot> findPosts(
            int page,
            int size,
            Status status,
            Post.Channel channel,
            String keyword
    ) {
        QPost post = QPost.post;
        Pageable pageable = PageRequest.of(page, size);
        BooleanBuilder condition = new BooleanBuilder();
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        if (status != null) {
            condition.and(post.status.eq(status));
        }

        if (channel != null) {
            condition.and(post.channel.eq(channel));
        }

        if (normalizedKeyword != null) {
            condition.and(post.content.containsIgnoreCase(normalizedKeyword));
        }

        List<PostSummarySnapshot> content = queryFactory
                .select(
                        post.id,
                        post.userId,
                        post.content,
                        post.channel,
                        post.status,
                        post.createdAt
                )
                .from(post)
                .where(condition)
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(tuple -> new PostSummarySnapshot(
                        tuple.get(post.id),
                        tuple.get(post.userId),
                        tuple.get(post.content),
                        tuple.get(post.channel),
                        tuple.get(post.status),
                        tuple.get(post.createdAt)
                ))
                .toList();

        Long totalCount = queryFactory
                .select(post.count())
                .from(post)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount == null ? 0L : totalCount);
    }

    public record PostSummarySnapshot(
            Long postId,
            Long authorUserId,
            String content,
            Post.Channel channel,
            Status status,
            LocalDateTime createdAt
    ) {
    }
}
