package com.beta.account.infra.repository;

import com.beta.account.domain.entity.QUser;
import com.beta.account.domain.entity.User;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChannelOverviewUserQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<TeamUserCountSnapshot> findActiveUserCountsByFavoriteTeamCode() {
        QUser user = QUser.user;
        StringExpression teamCodeExpr = user.baseballTeam.code;
        NumberExpression<Long> userCountExpr = user.id.count();

        return queryFactory
                .select(teamCodeExpr, userCountExpr)
                .from(user)
                .where(
                        user.status.eq(User.UserStatus.ACTIVE),
                        user.baseballTeam.isNotNull()
                )
                .groupBy(teamCodeExpr)
                .fetch()
                .stream()
                .map(tuple -> toSnapshot(tuple, teamCodeExpr, userCountExpr))
                .toList();
    }

    private TeamUserCountSnapshot toSnapshot(
            Tuple tuple,
            StringExpression teamCodeExpr,
            NumberExpression<Long> userCountExpr
    ) {
        String teamCode = tuple.get(teamCodeExpr);
        Long userCount = tuple.get(userCountExpr);

        return new TeamUserCountSnapshot(
                teamCode,
                userCount != null ? userCount : 0L
        );
    }

    public record TeamUserCountSnapshot(
            String teamCode,
            long userCount
    ) {
    }
}
