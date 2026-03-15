package com.beta.account.application.admin.dto;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserQueryRepository;

import java.time.LocalDateTime;

public record AdminUserQueryResult(
        Long userId,
        String nickname,
        String email,
        LocalDateTime joinedAt,
        SocialProvider socialProvider,
        String favoriteTeamName,
        User.GenderType gender,
        Integer age,
        String bio,
        User.UserStatus status
) {
    public static AdminUserQueryResult from(UserQueryRepository.UserSummarySnapshot snapshot) {
        return new AdminUserQueryResult(
                snapshot.userId(),
                snapshot.nickname(),
                snapshot.email(),
                snapshot.joinedAt(),
                snapshot.socialProvider(),
                snapshot.favoriteTeamName(),
                snapshot.gender(),
                snapshot.age(),
                snapshot.bio(),
                snapshot.status()
        );
    }
}
