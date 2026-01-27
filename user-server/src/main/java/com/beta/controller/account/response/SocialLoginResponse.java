package com.beta.controller.account.response;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.TeamDto;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.SignupStep;
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
            // 신규 사용자 또는 회원가입 진행 중인 사용자
            return SocialLoginResponse.builder()
                    .isNewUser(true)
                    .userResponse(new SignupInProgressResponse(
                            loginResult.getUserId(),
                            loginResult.getSignupStep() != null ? loginResult.getSignupStep().name() : SignupStep.SOCIAL_AUTHENTICATED.name(),
                            loginResult.getSocial()
                    ))
                    .build();
        } else {
            // 회원가입 완료된 기존 사용자
            return SocialLoginResponse.builder()
                    .isNewUser(false)
                    .userResponse(new ExistingUserResponse(
                            loginResult.getAccessToken(),
                            loginResult.getRefreshToken(),
                            loginResult.getDeviceId(),
                            loginResult.getUserInfo()
                    ))
                    .build();
        }
    }

    public interface UserResponse {}
    public record SignupInProgressResponse(Long userId, String signupStep, String social) implements UserResponse {}
    public record ExistingUserResponse(String accessToken, String refreshToken, String deviceId, UserDto user) implements UserResponse {}
}
