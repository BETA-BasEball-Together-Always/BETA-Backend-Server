package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.QComment;
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
public class AdminCommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<CommentSummarySnapshot> findComments(
            int page,
            int size,
            Status status,
            String keyword
    ) {
        QComment comment = QComment.comment;
        Pageable pageable = PageRequest.of(page, size);
        BooleanBuilder condition = new BooleanBuilder();
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        if (status != null) {
            condition.and(comment.status.eq(status));
        }

        if (normalizedKeyword != null) {
            condition.and(comment.content.containsIgnoreCase(normalizedKeyword));
        }

        List<CommentSummarySnapshot> content = queryFactory
                .select(
                        comment.id,
                        comment.userId,
                        comment.postId,
                        comment.content,
                        comment.depth,
                        comment.status,
                        comment.createdAt
                )
                .from(comment)
                .where(condition)
                .orderBy(comment.createdAt.desc(), comment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(tuple -> new CommentSummarySnapshot(
                        tuple.get(comment.id),
                        tuple.get(comment.userId),
                        tuple.get(comment.postId),
                        tuple.get(comment.content),
                        tuple.get(comment.depth),
                        tuple.get(comment.status),
                        tuple.get(comment.createdAt)
                ))
                .toList();

        Long totalCount = queryFactory
                .select(comment.count())
                .from(comment)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount == null ? 0L : totalCount);
    }

    public record CommentSummarySnapshot(
            Long commentId,
            Long authorUserId,
            Long postId,
            String content,
            Integer depth,
            Status status,
            LocalDateTime createdAt
    ) {
    }
}
