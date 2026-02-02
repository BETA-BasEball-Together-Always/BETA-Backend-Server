package com.beta.account.application.dto;

import com.beta.account.domain.entity.SignupStep;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResult {
    private final boolean isNewUser;
    private final boolean isNewDevice;
    private final Long userId;
    private final SignupStep signupStep;
    private final String accessToken;
    private final String refreshToken;
    private final String deviceId;
    private final UserDto userInfo;
    private final String social;
    private final List<TeamDto> teamList;

    public static LoginResult forSignupInProgress(Long userId, SignupStep signupStep, String social,
                                                  String accessToken, String refreshToken,
                                                  String deviceId, boolean isNewDevice) {
        return LoginResult.builder()
                .isNewUser(true)
                .isNewDevice(isNewDevice)
                .userId(userId)
                .signupStep(signupStep)
                .social(social)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .deviceId(deviceId)
                .userInfo(null)
                .build();
    }

    public static LoginResult forExistingUser(String accessToken, String refreshToken,
                                              String deviceId, boolean isNewDevice,
                                              UserDto user, String social) {
        return LoginResult.builder()
                .isNewUser(false)
                .isNewDevice(isNewDevice)
                .userId(user.getId())
                .signupStep(SignupStep.COMPLETED)
                .social(social)
                .teamList(null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .deviceId(deviceId)
                .userInfo(user)
                .build();
    }

    public static LoginResult forSignupComplete(Long userId, String accessToken, String refreshToken,
                                                UserDto user, String social) {
        return LoginResult.builder()
                .isNewUser(false)
                .isNewDevice(false)
                .userId(userId)
                .signupStep(SignupStep.COMPLETED)
                .social(social)
                .teamList(null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .deviceId(null)
                .userInfo(user)
                .build();
    }
}
