package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.QHashtag;
import com.beta.community.domain.entity.QPost;
import com.beta.community.domain.entity.Status;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PostMetricsSnapshot getDashboardPostMetricsSnapshot() {
        QPost post = QPost.post;
        LocalDateTime now = LocalDateTime.now(); // 조회 시점 현재
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay(); // 오늘 00:00:00
        LocalDateTime yesterdayStart = todayStart.minusDays(1); // 어제 00:00:00
        LocalDateTime yesterdayNow = now.minusDays(1); // 어제 동일 시각

        NumberExpression<Long> todayPostCountExpr = sumWhen(
                post.status.eq(Status.ACTIVE)
                        .and(post.createdAt.goe(todayStart))
                        .and(post.createdAt.lt(now)) // 오늘 게시물 수(ACTIVE)
        );
        NumberExpression<Long> yesterdaySameTimePostCountExpr = sumWhen(
                post.status.eq(Status.ACTIVE)
                        .and(post.createdAt.goe(yesterdayStart))
                        .and(post.createdAt.lt(yesterdayNow)) // 어제 동일 시각 게시물 수(ACTIVE)
        );

        Tuple tuple = queryFactory
                .select(todayPostCountExpr, yesterdaySameTimePostCountExpr)
                .from(post)
                .fetchOne();

        if (tuple == null) {
            return new PostMetricsSnapshot(0L, 0L);
        }

        return new PostMetricsSnapshot(
                getOrZero(tuple, todayPostCountExpr),
                getOrZero(tuple, yesterdaySameTimePostCountExpr)
        );
    }

    public List<Post> findRealtimeFeedPosts(int limit) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(post.status.eq(Status.ACTIVE))
                .orderBy(post.createdAt.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    public List<Hashtag> findPopularTopics(int limit) {
        QHashtag hashtag = QHashtag.hashtag;

        return queryFactory
                .selectFrom(hashtag)
                .where(hashtag.usageCount.gt(0L))
                .orderBy(hashtag.usageCount.desc(), hashtag.id.asc())
                .limit(limit)
                .fetch();
    }

    private NumberExpression<Long> sumWhen(BooleanExpression condition) {
        return new CaseBuilder()
                .when(condition).then(1L)
                .otherwise(0L)
                .sum();
    }

    private long getOrZero(Tuple tuple, NumberExpression<Long> expression) {
        Long value = tuple.get(expression);
        return value != null ? value : 0L;
    }

    public record PostMetricsSnapshot(
            long todayPostCount,
            long yesterdaySameTimePostCount
    ) {
    }
}
