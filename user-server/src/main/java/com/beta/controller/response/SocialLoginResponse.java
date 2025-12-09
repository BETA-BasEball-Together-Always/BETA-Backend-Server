package com.beta.controller.response;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.TeamDto;
import com.beta.account.application.dto.UserDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SocialLoginResponse {

    private boolean isNewUser;
    private UserResponse userResponse;

    public static SocialLoginResponse ofLoginResult(LoginResult loginResult) {
        if (loginResult.isNewUser()) {
            return SocialLoginResponse.builder()
                    .isNewUser(true)
                    .userResponse(new NewUserResponse(loginResult.getSocial(), loginResult.getTeamList()))
                    .build();
        } else {
            return SocialLoginResponse.builder()
                    .isNewUser(false)
                    .userResponse(new ExistingUserResponse(
                            loginResult.getAccessToken(),
                            loginResult.getRefreshToken(),
                            loginResult.getUserInfo()
                    ))
                    .build();
        }
    }

    private interface UserResponse {}
    private record NewUserResponse(String social, List<TeamDto> teamList) implements UserResponse {}
    private record ExistingUserResponse(String accessToken, String refreshToken, UserDto user) implements UserResponse {}
}
