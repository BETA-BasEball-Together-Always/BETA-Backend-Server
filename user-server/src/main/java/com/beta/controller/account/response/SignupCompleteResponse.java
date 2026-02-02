package com.beta.controller.account.response;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.UserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupCompleteResponse {

    private String accessToken;
    private String refreshToken;
    private UserDto user;

    public static SignupCompleteResponse from(LoginResult result) {
        return SignupCompleteResponse.builder()
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .user(result.getUserInfo())
                .build();
    }
}
