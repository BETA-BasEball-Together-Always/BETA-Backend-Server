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
public class ChannelOverviewMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<TeamMemberCountSnapshot> findActiveMemberCountsByFavoriteTeamCode() {
        QUser user = QUser.user;
        StringExpression teamCodeExpr = user.baseballTeam.code;
        NumberExpression<Long> memberCountExpr = user.id.count();

        return queryFactory
                .select(teamCodeExpr, memberCountExpr)
                .from(user)
                .where(
                        user.status.eq(User.UserStatus.ACTIVE),
                        user.baseballTeam.isNotNull()
                )
                .groupBy(teamCodeExpr)
                .fetch()
                .stream()
                .map(tuple -> toSnapshot(tuple, teamCodeExpr, memberCountExpr))
                .toList();
    }

    private TeamMemberCountSnapshot toSnapshot(
            Tuple tuple,
            StringExpression teamCodeExpr,
            NumberExpression<Long> memberCountExpr
    ) {
        String teamCode = tuple.get(teamCodeExpr);
        Long memberCount = tuple.get(memberCountExpr);

        return new TeamMemberCountSnapshot(
                teamCode,
                memberCount != null ? memberCount : 0L
        );
    }

    public record TeamMemberCountSnapshot(
            String teamCode,
            long memberCount
    ) {
    }
}
