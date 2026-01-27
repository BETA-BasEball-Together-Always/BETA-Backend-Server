package com.beta.controller.account.response;

import com.beta.account.application.dto.TokenDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;

    public static TokenResponse from(TokenDto tokenDto) {
        return TokenResponse.builder()
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .build();
    }
}
