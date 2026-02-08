package com.beta.account.application;

import com.beta.account.application.dto.*;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.SignupStep;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.*;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.exception.account.SocialEmailNotProvidedException;
import com.beta.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountAppService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final RefreshTokenService refreshTokenService;
    private final SocialUserInfoService socialUserInfoService;
    private final UserStatusService userStatusService;
    private final BaseballTeamReadService baseballTeamReadService;
    private final WelcomeEmailService welcomeEmailService;
    private final UserDeviceWriteService userDeviceWriteService;
    private final DeviceAppService deviceAppService;

    public LoginResult processSocialLogin(String token, SocialProvider socialProvider, String deviceId) {
        SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(token, socialProvider);
        User user = userReadService.findUserBySocialId(socialUserInfo.getSocialId(), socialProvider);

        if (user == null) {
            String email = socialUserInfo.getEmail();
            if (email == null || email.isBlank()) {
                throw new SocialEmailNotProvidedException("소셜 계정에 등록된 이메일이 없습니다. 이메일이 등록된 계정으로 시도해주세요.");
            }
            userStatusService.validateEmailDuplicate(email);

            User newUser = userWriteService.createSocialUser(socialUserInfo.getSocialId(), socialProvider, email);
            boolean isNewDevice = deviceAppService.registerOrUpdateDevice(newUser.getId(), deviceId, null);
            String accessToken = jwtTokenProvider.generateAccessToken(newUser.getId(), null, "USER");
            String refreshToken = UUID.randomUUID().toString();
            refreshTokenService.upsertRefreshToken(newUser.getId(), refreshToken);

            return LoginResult.forSignupInProgress(
                    newUser.getId(), SignupStep.SOCIAL_AUTHENTICATED, socialProvider.name(),
                    accessToken, refreshToken, deviceId, isNewDevice
            );
        }
        if (!user.isSignupCompleted()) {
            boolean isNewDevice = deviceAppService.registerOrUpdateDevice(user.getId(), deviceId, null);
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), null, "USER");
            String refreshToken = UUID.randomUUID().toString();
            refreshTokenService.upsertRefreshToken(user.getId(), refreshToken);

            return LoginResult.forSignupInProgress(
                    user.getId(), user.getSignupStep(), socialProvider.name(),
                    accessToken, refreshToken, deviceId, isNewDevice
            );
        }

        userStatusService.validateUserStatus(user);
        boolean isNewDevice = deviceAppService.registerOrUpdateDevice(user.getId(), deviceId, null);
        return createLoginResult(
                user.getId(),
                user.getBaseballTeam().getCode(),
                user.getRole().name(),
                UserDto.toDto(user),
                socialProvider.name(),
                deviceId,
                isNewDevice
        );
    }

    public boolean isNameDuplicate(String nickName) {
        return userStatusService.isNameDuplicate(nickName);
    }

    public TokenDto refreshTokens(String refreshToken) {
        Long userId = refreshTokenService.findUserIdByToken(refreshToken);

        User user = userReadService.findUserById(userId);
        userStatusService.validateUserStatus(user);

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getBaseballTeam().getCode(),
                user.getRole().name()
        );
        String newRefreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(user.getId(), newRefreshToken);

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken).build();
    }

    public void logout(Long userId, String deviceId) {
        refreshTokenService.deleteByUserId(userId);
        userDeviceWriteService.deleteByDeviceId(userId, deviceId);
    }

    public SignupStepResult processConsent(Long userId, Boolean personalInfoRequired, Boolean agreeMarketing) {
        User user = userReadService.findUserById(userId);
        userStatusService.validateSignupStep(user, SignupStep.SOCIAL_AUTHENTICATED);
        userStatusService.validateAgreePersonalInfo(personalInfoRequired);

        userWriteService.processConsent(userId, agreeMarketing, personalInfoRequired);
        return SignupStepResult.withEmail(userId, SignupStep.CONSENT_AGREED, user.getEmail());
    }

    public SignupStepResult processProfile(Long userId, String nickname) {
        User user = userReadService.findUserById(userId);
        userStatusService.validateSignupStep(user, SignupStep.CONSENT_AGREED);
        userStatusService.validateNicknameLength(nickname);
        userStatusService.validateNameDuplicate(nickname);

        userWriteService.updateProfile(userId, nickname);
        List<BaseballTeam> teamList = baseballTeamReadService.getAllBaseballTeams();
        return SignupStepResult.withTeamList(userId, SignupStep.PROFILE_COMPLETED, teamList);
    }

    public SignupStepResult processTeamSelection(Long userId, String teamCode) {
        User user = userReadService.findUserById(userId);
        userStatusService.validateSignupStep(user, SignupStep.PROFILE_COMPLETED);
        BaseballTeam baseballTeam = baseballTeamReadService.getBaseballTeamById(teamCode);

        userWriteService.updateTeam(userId, baseballTeam);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, teamCode, user.getRole().name());
        return SignupStepResult.withAccessToken(userId, SignupStep.TEAM_SELECTED, newAccessToken);
    }

    public LoginResult completeSignup(Long userId) {
        User user = userReadService.findUserById(userId);
        userStatusService.validateSignupStep(user, SignupStep.TEAM_SELECTED);

        User completedUser = userWriteService.completeSignup(userId);
        welcomeEmailService.sendWelcomeEmail(completedUser.getEmail(), completedUser.getNickname());

        return createSignupCompleteResult(completedUser);
    }

    public LoginResult completeSignupWithInfo(Long userId, String gender, Integer age) {
        User user = userReadService.findUserById(userId);
        userStatusService.validateSignupStep(user, SignupStep.TEAM_SELECTED);

        User.GenderType genderType = gender != null ? User.GenderType.valueOf(gender) : null;
        User completedUser = userWriteService.completeSignupWithInfo(userId, genderType, age);
        welcomeEmailService.sendWelcomeEmail(completedUser.getEmail(), completedUser.getNickname());

        return createSignupCompleteResult(completedUser);
    }

    private LoginResult createLoginResult(Long userId, String favoriteTeamCode, String role,
                                          UserDto user, String social, String deviceId, boolean isNewDevice) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, favoriteTeamCode, role);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(userId, refreshToken);

        return LoginResult.forExistingUser(accessToken, refreshToken, deviceId, isNewDevice, user, social);
    }

    private LoginResult createSignupCompleteResult(User completedUser) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                completedUser.getId(),
                completedUser.getBaseballTeam().getCode(),
                completedUser.getRole().name()
        );
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(completedUser.getId(), refreshToken);

        return LoginResult.forSignupComplete(
                completedUser.getId(),
                accessToken,
                refreshToken,
                UserDto.toDto(completedUser),
                completedUser.getSocialProvider().name()
        );
    }
}
