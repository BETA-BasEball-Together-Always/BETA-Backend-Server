package com.beta.account.application.admin;

import com.beta.account.application.admin.dto.AdminLoginResult;
import com.beta.account.application.admin.dto.AdminTokenResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.AdminRefreshTokenService;
import com.beta.account.domain.service.SocialUserInfoService;
import com.beta.account.domain.service.UserReadService;
import com.beta.account.domain.service.UserStatusService;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.security.AdminAuthConstants;
import com.beta.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuthFacadeService {

    private final SocialUserInfoService socialUserInfoService;
    private final UserReadService userReadService;
    private final UserStatusService userStatusService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRefreshTokenService adminRefreshTokenService;

    @Transactional(readOnly = true)
    public AdminLoginResult loginWithKakao(String token) {
        SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(token, SocialProvider.KAKAO);
        User user = userReadService.findUserBySocialId(socialUserInfo.getSocialId(), SocialProvider.KAKAO);

        userStatusService.validateAdminUser(user);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                null,
                AdminAuthConstants.ADMIN_ROLE,
                AdminAuthConstants.ADMIN_CLIENT
        );
        String refreshToken = UUID.randomUUID().toString();
        adminRefreshTokenService.upsertRefreshToken(user.getId(), refreshToken);

        return AdminLoginResult.from(user, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AdminTokenResult refreshTokens(String refreshToken) {
        Long userId = adminRefreshTokenService.findUserIdByToken(refreshToken);
        User user = userReadService.findUserById(userId);

        userStatusService.validateAdminUser(user);

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                null,
                AdminAuthConstants.ADMIN_ROLE,
                AdminAuthConstants.ADMIN_CLIENT
        );
        String newRefreshToken = UUID.randomUUID().toString();
        adminRefreshTokenService.upsertRefreshToken(user.getId(), newRefreshToken);

        return AdminTokenResult.from(newAccessToken, newRefreshToken);
    }

    public void logout(Long userId) {
        adminRefreshTokenService.deleteByUserId(userId);
    }
}
