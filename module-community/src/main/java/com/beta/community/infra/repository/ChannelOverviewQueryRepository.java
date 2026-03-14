package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.QComment;
import com.beta.community.domain.entity.QPost;
import com.beta.community.domain.entity.Status;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChannelOverviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ChannelDailyCountSnapshot> findDailyPostCounts(LocalDate today, int days) {
        QPost post = QPost.post;
        QueryRange range = resolveRange(today, days);
        StringExpression channelCodeExpr = post.channel.stringValue();
        StringTemplate dateExpr = Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", post.createdAt);
        NumberExpression<Long> countExpr = post.id.count();

        return queryFactory
                .select(channelCodeExpr, dateExpr, countExpr)
                .from(post)
                .where(
                        post.status.eq(Status.ACTIVE),
                        post.channel.ne(Post.Channel.ALL),
                        post.createdAt.goe(range.startAt()),
                        post.createdAt.lt(range.endAt())
                )
                .groupBy(post.channel, dateExpr)
                .fetch()
                .stream()
                .map(tuple -> toSnapshot(tuple, channelCodeExpr, dateExpr, countExpr))
                .toList();
    }

    public List<ChannelDailyCountSnapshot> findDailyCommentCounts(LocalDate today, int days) {
        QComment comment = QComment.comment;
        QPost post = QPost.post;
        QueryRange range = resolveRange(today, days);
        StringExpression channelCodeExpr = post.channel.stringValue();
        StringTemplate dateExpr = Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", comment.createdAt);
        NumberExpression<Long> countExpr = comment.id.count();

        return queryFactory
                .select(channelCodeExpr, dateExpr, countExpr)
                .from(comment)
                .join(post).on(comment.postId.eq(post.id))
                .where(
                        comment.status.eq(Status.ACTIVE),
                        post.status.eq(Status.ACTIVE),
                        post.channel.ne(Post.Channel.ALL),
                        comment.createdAt.goe(range.startAt()),
                        comment.createdAt.lt(range.endAt())
                )
                .groupBy(post.channel, dateExpr)
                .fetch()
                .stream()
                .map(tuple -> toSnapshot(tuple, channelCodeExpr, dateExpr, countExpr))
                .toList();
    }

    private QueryRange resolveRange(LocalDate today, int days) {
        LocalDate startDate = today.minusDays(days - 1L);

        return new QueryRange(
                startDate.atStartOfDay(),
                today.plusDays(1L).atStartOfDay()
        );
    }

    private ChannelDailyCountSnapshot toSnapshot(
            Tuple tuple,
            StringExpression channelCodeExpr,
            StringTemplate dateExpr,
            NumberExpression<Long> countExpr
    ) {
        String channelCode = tuple.get(channelCodeExpr);
        String dateKey = tuple.get(dateExpr);
        Long count = tuple.get(countExpr);

        return new ChannelDailyCountSnapshot(
                channelCode,
                LocalDate.parse(dateKey),
                count != null ? count : 0L
        );
    }

    public record ChannelDailyCountSnapshot(
            String channelCode,
            LocalDate date,
            long count
    ) {
    }

    private record QueryRange(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
    }
}
