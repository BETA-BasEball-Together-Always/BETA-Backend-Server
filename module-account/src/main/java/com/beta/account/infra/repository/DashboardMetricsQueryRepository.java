package com.beta.account.infra.repository;

import com.beta.account.domain.entity.QUser;
import com.beta.account.domain.entity.User;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class DashboardMetricsQueryRepository {

    private final JPAQueryFactory queryFactory;

    public DashboardMetricsSnapshot getDashboardMetricsSnapshot() {
        QUser user = QUser.user;
        LocalDateTime now = LocalDateTime.now(); // 조회 시점 현재
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay(); // 오늘 00:00:00
        LocalDateTime yesterdayStart = todayStart.minusDays(1); // 어제 00:00:00
        LocalDateTime yesterdayNow = now.minusDays(1); // 어제 동일 시각

        NumberExpression<Long> totalUserCountExpr = sumWhen(
                user.status.eq(User.UserStatus.ACTIVE) // 총 사용자 수(ACTIVE)
        );
        NumberExpression<Long> todayActiveSignupsExpr = sumWhen(
                user.status.eq(User.UserStatus.ACTIVE)
                        .and(user.createdAt.goe(todayStart))
                        .and(user.createdAt.lt(now)) // 오늘 ACTIVE 가입 수
        );
        NumberExpression<Long> todayWithdrawnUsersExpr = sumWhen(
                user.withdrawnAt.goe(todayStart)
                        .and(user.withdrawnAt.lt(now)) // 오늘 탈퇴 수
        );
        NumberExpression<Long> todayNewSignupCountExpr = sumWhen(
                user.createdAt.goe(todayStart)
                        .and(user.createdAt.lt(now)) // 오늘 신규 가입 수
        );
        NumberExpression<Long> yesterdayNewSignupCountExpr = sumWhen(
                user.createdAt.goe(yesterdayStart)
                        .and(user.createdAt.lt(yesterdayNow)) // 어제 동일 시각까지 가입 수
        );

        Tuple tuple = queryFactory
                .select(
                        totalUserCountExpr,
                        todayActiveSignupsExpr,
                        todayWithdrawnUsersExpr,
                        todayNewSignupCountExpr,
                        yesterdayNewSignupCountExpr
                )
                .from(user)
                .fetchOne();

        if (tuple == null) {
            return new DashboardMetricsSnapshot(0L, 0L, 0L, 0L, 0L);
        }

        return new DashboardMetricsSnapshot(
                getOrZero(tuple, totalUserCountExpr),
                getOrZero(tuple, todayActiveSignupsExpr),
                getOrZero(tuple, todayWithdrawnUsersExpr),
                getOrZero(tuple, todayNewSignupCountExpr),
                getOrZero(tuple, yesterdayNewSignupCountExpr)
        );
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

    public record DashboardMetricsSnapshot(
            long totalUserCount,
            long todayActiveSignups,
            long todayWithdrawnUsers,
            long todayNewSignupCount,
            long yesterdayNewSignupCount
    ) {
    }
}
