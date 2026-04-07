package com.beta.account.infra.repository;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.QUser;
import com.beta.account.domain.entity.User;
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
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<UserSummarySnapshot> findUsersExcludingAdmins(
            int page,
            int size,
            User.UserStatus status,
            String keyword
    ) {
        QUser user = QUser.user;
        Pageable pageable = PageRequest.of(page, size);
        BooleanBuilder condition = new BooleanBuilder();
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        condition.and(user.role.eq(User.UserRole.USER));

        if (status != null) {
            condition.and(user.status.eq(status));
        }

        if (normalizedKeyword != null) {
            condition.and(
                    user.nickname.containsIgnoreCase(normalizedKeyword)
                            .or(user.email.containsIgnoreCase(normalizedKeyword))
            );
        }

        List<UserSummarySnapshot> content = queryFactory
                .select(
                        user.id,
                        user.nickname,
                        user.email,
                        user.createdAt,
                        user.socialProvider,
                        user.baseballTeam.teamNameKr,
                        user.bio,
                        user.status
                )
                .from(user)
                .leftJoin(user.baseballTeam)
                .where(condition)
                .orderBy(user.createdAt.desc(), user.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(tuple -> new UserSummarySnapshot(
                        tuple.get(user.id),
                        tuple.get(user.nickname),
                        tuple.get(user.email),
                        tuple.get(user.createdAt),
                        tuple.get(user.socialProvider),
                        tuple.get(user.baseballTeam.teamNameKr),
                        tuple.get(user.bio),
                        tuple.get(user.status)
                ))
                .toList();

        Long totalCount = queryFactory
                .select(user.count())
                .from(user)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount == null ? 0L : totalCount);
    }

    public record UserSummarySnapshot(
            Long userId,
            String nickname,
            String email,
            LocalDateTime joinedAt,
            SocialProvider socialProvider,
            String favoriteTeamName,
            String bio,
            User.UserStatus status
    ) {
    }
}
