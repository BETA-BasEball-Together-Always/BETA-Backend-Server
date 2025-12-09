package com.beta.account.application;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.BaseballTeamReadService;
import com.beta.account.domain.service.RefreshTokenService;
import com.beta.account.domain.service.SocialUserInfoService;
import com.beta.account.domain.service.SocialUserStatusService;
import com.beta.account.domain.service.UserReadService;
import com.beta.account.infra.client.SocialUserInfo;
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
    private final RefreshTokenService refreshTokenService;
    private final SocialUserInfoService socialUserInfoService;
    private final SocialUserStatusService socialUserStatusService;
    private final BaseballTeamReadService baseballTeamReadService;

    public LoginResult processSocialLogin(String token, SocialProvider socialProvider) {
        SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(token, socialProvider);

        User user = userReadService.findUserBySocialId(socialUserInfo.getSocialId(), socialProvider);

        if (user == null) {
            List<BaseballTeam> teamList = baseballTeamReadService.getAllBaseballTeams();
            return LoginResult.forNewUser(true, teamList, socialProvider.name());
        } else{
            socialUserStatusService.validateUserStatus(user);
            return createLoginResult(
                    user.getId(),
                    user.getBaseballTeam().getCode(),
                    user.getRole().name(),
                    UserDto.toDto(user),
                    socialProvider.name()
            );
        }
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
