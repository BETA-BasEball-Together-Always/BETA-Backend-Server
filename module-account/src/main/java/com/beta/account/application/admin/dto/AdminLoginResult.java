package com.beta.account.application.admin.dto;

import com.beta.account.domain.entity.User;

public record AdminLoginResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String nickname,
        String role
) {

    public static AdminLoginResult from(User user, String accessToken, String refreshToken) {
        return new AdminLoginResult(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
