package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.QPost;
import com.beta.community.domain.entity.Status;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public static final int PAGE_SIZE = 10;

    public List<Post> findPostsWithCursor(Long cursor, String channel, List<Long> blockedUserIds) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        cursorCondition(cursor),
                        channelCondition(channel),
                        blockedUserCondition(blockedUserIds)
                )
                .orderBy(post.id.desc())
                .limit(PAGE_SIZE + 1)
                .fetch();
    }

    public List<Post> findPostsWithOffset(int offset, String channel, List<Long> blockedUserIds) {
        QPost post = QPost.post;

        NumberExpression<Integer> totalEmotions = post.likeCount
                .add(post.sadCount)
                .add(post.funCount)
                .add(post.hypeCount);

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        channelCondition(channel),
                        blockedUserCondition(blockedUserIds)
                )
                .orderBy(totalEmotions.desc(), post.id.desc())
                .offset(offset)
                .limit(PAGE_SIZE + 1)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return QPost.post.id.lt(cursor);
    }

    private BooleanExpression channelCondition(String channel) {
        if (channel == null) {
            return null;
        }
        return QPost.post.channel.eq(Post.Channel.valueOf(channel));
    }

    private BooleanExpression blockedUserCondition(List<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return null;
        }
        return QPost.post.userId.notIn(blockedUserIds);
    }

    public List<Post> findPostsByUserId(Long userId, Long cursor, int limit) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        post.userId.eq(userId),
                        cursorCondition(cursor)
                )
                .orderBy(post.id.desc())
                .limit(limit + 1)
                .fetch();
    }

    public List<Post> findPostsByIdsWithCursor(List<Long> postIds, Long cursor, int limit) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }

        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        post.id.in(postIds),
                        cursorCondition(cursor)
                )
                .orderBy(post.id.desc())
                .limit(limit + 1)
                .fetch();
    }

    public List<Post> findPostsByUserIdWithChannelFilter(Long userId, List<String> channels, Long cursor, int limit) {
        QPost post = QPost.post;

        List<Post.Channel> channelEnums = channels.stream()
                .map(Post.Channel::valueOf)
                .toList();

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        post.userId.eq(userId),
                        post.channel.in(channelEnums),
                        cursorCondition(cursor)
                )
                .orderBy(post.id.desc())
                .limit(limit + 1)
                .fetch();
    }

    public List<Post> findPopularPostsWithinHours(int hours, int limit, List<Long> blockedUserIds) {
        QPost post = QPost.post;
        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        NumberExpression<Integer> totalEmotions = post.likeCount
                .add(post.sadCount)
                .add(post.funCount)
                .add(post.hypeCount);

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        post.channel.eq(Post.Channel.ALL),
                        post.createdAt.goe(since),
                        blockedUserCondition(blockedUserIds)
                )
                .orderBy(totalEmotions.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }
}
