package com.beta.account.infra.repository;

import com.beta.account.domain.entity.QUser;
import com.beta.account.domain.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminUserStatisticsQueryRepository {

    private final JPAQueryFactory queryFactory;

    public UserStatisticsSnapshot getUserStatistics(User.UserStatus status) {
        QUser user = QUser.user;
        BooleanBuilder condition = buildCondition(user, status);

        Long totalUserCount = queryFactory
                .select(user.count())
                .from(user)
                .where(condition)
                .fetchOne();

        return new UserStatisticsSnapshot(
                totalUserCount != null ? totalUserCount : 0L,
                findGenderStats(user, condition),
                findAgeStats(user, condition)
        );
    }

    private BooleanBuilder buildCondition(QUser user, User.UserStatus status) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(user.role.eq(User.UserRole.USER));

        if (status != null) {
            condition.and(user.status.eq(status));
        }

        return condition;
    }

    private List<GenderCountSnapshot> findGenderStats(QUser user, BooleanBuilder condition) {
        NumberExpression<Long> femaleCountExpr = sumWhen(user.gender.eq(User.GenderType.F));
        NumberExpression<Long> maleCountExpr = sumWhen(user.gender.eq(User.GenderType.M));
        NumberExpression<Long> unspecifiedCountExpr = sumWhen(user.gender.isNull());

        Tuple tuple = queryFactory
                .select(femaleCountExpr, maleCountExpr, unspecifiedCountExpr)
                .from(user)
                .where(condition)
                .fetchOne();

        return List.of(
                new GenderCountSnapshot(GenderStatType.FEMALE, getOrZero(tuple, femaleCountExpr)),
                new GenderCountSnapshot(GenderStatType.MALE, getOrZero(tuple, maleCountExpr)),
                new GenderCountSnapshot(GenderStatType.UNSPECIFIED, getOrZero(tuple, unspecifiedCountExpr))
        );
    }

    private List<AgeGroupCountSnapshot> findAgeStats(QUser user, BooleanBuilder condition) {
        NumberExpression<Long> teensCountExpr = sumWhen(user.age.between(10, 19));
        NumberExpression<Long> twentiesCountExpr = sumWhen(user.age.between(20, 29));
        NumberExpression<Long> thirtiesCountExpr = sumWhen(user.age.between(30, 39));
        NumberExpression<Long> fortiesCountExpr = sumWhen(user.age.between(40, 49));
        NumberExpression<Long> fiftiesCountExpr = sumWhen(user.age.between(50, 59));
        NumberExpression<Long> othersCountExpr = sumWhen(
                user.age.isNotNull().and(user.age.lt(10).or(user.age.goe(60)))
        );
        NumberExpression<Long> unspecifiedCountExpr = sumWhen(user.age.isNull());

        Tuple tuple = queryFactory
                .select(
                        teensCountExpr,
                        twentiesCountExpr,
                        thirtiesCountExpr,
                        fortiesCountExpr,
                        fiftiesCountExpr,
                        othersCountExpr,
                        unspecifiedCountExpr
                )
                .from(user)
                .where(condition)
                .fetchOne();

        return List.of(
                new AgeGroupCountSnapshot(AgeGroupStatType.TEENS, getOrZero(tuple, teensCountExpr)),
                new AgeGroupCountSnapshot(AgeGroupStatType.TWENTIES, getOrZero(tuple, twentiesCountExpr)),
                new AgeGroupCountSnapshot(AgeGroupStatType.THIRTIES, getOrZero(tuple, thirtiesCountExpr)),
                new AgeGroupCountSnapshot(AgeGroupStatType.FORTIES, getOrZero(tuple, fortiesCountExpr)),
                new AgeGroupCountSnapshot(AgeGroupStatType.FIFTIES, getOrZero(tuple, fiftiesCountExpr)),
                new AgeGroupCountSnapshot(AgeGroupStatType.OTHERS, getOrZero(tuple, othersCountExpr)),
                new AgeGroupCountSnapshot(AgeGroupStatType.UNSPECIFIED, getOrZero(tuple, unspecifiedCountExpr))
        );
    }

    private NumberExpression<Long> sumWhen(BooleanExpression condition) {
        return new CaseBuilder()
                .when(condition).then(1L)
                .otherwise(0L)
                .sum();
    }

    private long getOrZero(Tuple tuple, NumberExpression<Long> expression) {
        if (tuple == null) {
            return 0L;
        }

        Long value = tuple.get(expression);
        return value != null ? value : 0L;
    }

    public record UserStatisticsSnapshot(
            long totalUserCount,
            List<GenderCountSnapshot> genderStats,
            List<AgeGroupCountSnapshot> ageStats
    ) {
    }

    public record GenderCountSnapshot(
            GenderStatType gender,
            long count
    ) {
    }

    public record AgeGroupCountSnapshot(
            AgeGroupStatType ageGroup,
            long count
    ) {
    }

    public enum GenderStatType {
        FEMALE,
        MALE,
        UNSPECIFIED
    }

    public enum AgeGroupStatType {
        TEENS,
        TWENTIES,
        THIRTIES,
        FORTIES,
        FIFTIES,
        OTHERS,
        UNSPECIFIED
    }
}
