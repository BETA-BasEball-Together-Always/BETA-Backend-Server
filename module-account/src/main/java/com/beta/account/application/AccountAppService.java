package com.beta.account.application;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.*;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public LoginResult processSocialLogin(String token, SocialProvider socialProvider) {
        SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(token, socialProvider);
        User user = userReadService.findUserBySocialId(socialUserInfo.getSocialId(), socialProvider);

        if (user == null) { // 신규 사용자
            List<BaseballTeam> teamList = baseballTeamReadService.getAllBaseballTeams();
            return LoginResult.forNewUser(true, teamList, socialProvider.name());
        } else{ // 기존 사용자
            userStatusService.validateUserStatus(user);
            return createLoginResult(
                    user.getId(),
                    user.getBaseballTeam().getCode(),
                    user.getRole().name(),
                    UserDto.toDto(user),
                    socialProvider.name()
            );
        }
    }

    public LoginResult completeSignup(UserDto user, Boolean agreeMarketing, Boolean personalInfoRequired, String socialToken) {
        validateAccount(user, personalInfoRequired);
        UserDto saveUser = saveAccount(user, agreeMarketing, personalInfoRequired, socialToken);
        return createLoginResult(
                saveUser.getId(),
                saveUser.getFavoriteTeamCode(),
                saveUser.getRole(),
                saveUser,
                saveUser.getSocialProvider().name()
        );
    }

    private UserDto saveAccount(UserDto user, Boolean agreeMarketing, Boolean personalInfoRequired, String socialToken) {
        UserDto saveUser = userWriteService.saveUser(getNewUser(user, socialToken));
        userWriteService.saveAgreements(agreeMarketing, personalInfoRequired, saveUser.getId());
        return saveUser;
    }

    private User getNewUser(UserDto user, String socialToken) {
        String socialId = null;
        SocialProvider socialProvider = SocialProvider.EMAIL;

        if (socialToken != null) {
            socialProvider = user.getSocialProvider();
            socialId = socialUserInfoService.fetchSocialUserInfo(socialToken, socialProvider).getSocialId();
        }

        BaseballTeam baseballTeam = baseballTeamReadService.getBaseballTeamById(user.getFavoriteTeamCode());
        String password = passwordEncoder.encode(user.getPassword());
        return User.createNewUser(user, password, baseballTeam, socialId, socialProvider);
    }

    private void validateAccount(UserDto user, Boolean personalInfoRequired) {
        userStatusService.isEmailDuplicate(user.getEmail());
        userStatusService.isNameDuplicate(user.getNickname());
        userStatusService.validateAgreePersonalInfo(personalInfoRequired);
    }

    private LoginResult createLoginResult(Long userId, String favoriteTeamCode, String role, UserDto user, String social) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, favoriteTeamCode, role);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(userId, refreshToken);
        return LoginResult.forExistingUser(
                false, accessToken, refreshToken, user, social
        );
    }
}
