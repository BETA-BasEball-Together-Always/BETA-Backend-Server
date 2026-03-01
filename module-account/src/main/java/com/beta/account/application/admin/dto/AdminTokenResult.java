package com.beta.account.application.admin.dto;

public record AdminTokenResult(
        String accessToken,
        String refreshToken
) {

    public static AdminTokenResult from(String accessToken, String refreshToken) {
        return new AdminTokenResult(accessToken, refreshToken);
    }
}
