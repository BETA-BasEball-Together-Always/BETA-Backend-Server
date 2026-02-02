package com.beta.controller.account.response;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.SignupStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginResponse {

    private boolean isNewUser;
    private UserResponse userResponse;

    public static SocialLoginResponse ofLoginResult(LoginResult loginResult) {
        if (loginResult.isNewUser()) {
            return SocialLoginResponse.builder()
                    .isNewUser(true)
                    .userResponse(new SignupInProgressResponse(
                            loginResult.getAccessToken(),
                            loginResult.getRefreshToken(),
                            loginResult.getDeviceId(),
                            loginResult.isNewDevice(),
                            loginResult.getSignupStep() != null ? loginResult.getSignupStep().name() : SignupStep.SOCIAL_AUTHENTICATED.name(),
                            loginResult.getSocial()
                    ))
                    .build();
        } else {
            return SocialLoginResponse.builder()
                    .isNewUser(false)
                    .userResponse(new ExistingUserResponse(
                            loginResult.getAccessToken(),
                            loginResult.getRefreshToken(),
                            loginResult.getDeviceId(),
                            loginResult.isNewDevice(),
                            loginResult.getUserInfo()
                    ))
                    .build();
        }
    }

    public interface UserResponse {}
    public record SignupInProgressResponse(String accessToken, String refreshToken, String deviceId,
                                           boolean isNewDevice, String signupStep, String social) implements UserResponse {}
    public record ExistingUserResponse(String accessToken, String refreshToken, String deviceId,
                                       boolean isNewDevice, UserDto user) implements UserResponse {}
}
