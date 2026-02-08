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

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final int PAGE_SIZE = 10;

    public List<Post> findPostsWithCursor(Long cursor, String channel, List<Long> blockedUserIds, String sort) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        cursorCondition(cursor, sort),
                        channelCondition(channel),
                        blockedUserCondition(blockedUserIds)
                )
                .orderBy(getOrderSpecifiers(sort))
                .limit(PAGE_SIZE + 1)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor, String sort) {
        if (cursor == null) {
            return null;
        }
        if ("popular".equalsIgnoreCase(sort)) {
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

    private OrderSpecifier<?>[] getOrderSpecifiers(String sort) {
        QPost post = QPost.post;

        if ("popular".equalsIgnoreCase(sort)) {
            NumberExpression<Integer> totalEmotions = post.likeCount
                    .add(post.sadCount)
                    .add(post.funCount)
                    .add(post.hypeCount);
            return new OrderSpecifier<?>[] {
                    totalEmotions.desc(),
                    post.id.desc()
            };
        }
        return new OrderSpecifier<?>[] { post.id.desc() };
    }
}
