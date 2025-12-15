package com.beta.account.application;

import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.TokenDto;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.*;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.exception.account.SamePasswordException;
import com.beta.core.security.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;
    private final PasswordCodeService passwordCodeService;
    private final PasswordEmailService passwordEmailService;

    /*====================AuthController======================*/
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

    public LoginResult processEmailLogin(String email, String password) {
        User user = userReadService.findUserByEmail(email);
        userStatusService.validateUserStatus(user);
        userStatusService.validatePasswordExistence(user.getPassword(), password);

        return createLoginResult(
                user.getId(),
                user.getBaseballTeam().getCode(),
                user.getRole().name(),
                UserDto.toDto(user),
                "EMAIL"
        );
    }

    public boolean isNameDuplicate(String nickName) {
        return userStatusService.isNameDuplicate(nickName);
    }

    public boolean isEmailDuplicate(String email) {
        return userStatusService.isEmailDuplicate(email);
    }

    public LoginResult completeSignup(UserDto user, Boolean agreeMarketing, Boolean personalInfoRequired, String socialToken) {
        validateAccount(user, personalInfoRequired);
        UserDto saveUser = saveAccount(user, agreeMarketing, personalInfoRequired, socialToken);
        welcomeEmailService.sendWelcomeEmail(saveUser.getEmail(), saveUser.getNickname());
        return createLoginResult(
                saveUser.getId(),
                saveUser.getFavoriteTeamCode(),
                saveUser.getRole(),
                saveUser,
                saveUser.getSocialProvider().name()
        );
    }

    @Transactional
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

    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
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
        userStatusService.validateEmailDuplicate(user.getEmail());
        userStatusService.validateNameDuplicate(user.getNickname());
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

    /*==============================PassController=======================================*/
    public boolean sendPasswordResetCode(@NotBlank String email) {
        User user = userReadService.findUserByEmail(email);
        userStatusService.validateUserStatus(user);
        String verificationCode = passwordCodeService.generateAndSaveVerificationCode(user.getId());
        passwordEmailService.sendPasswordCord(user.getEmail(), user.getNickname(), verificationCode);
        return true;
    }

    public boolean verifyPasswordResetCode(@NotBlank String email, @NotBlank String code) {
        User user = userReadService.findUserByEmail(email);
        userStatusService.validateUserStatus(user);
        passwordCodeService.verifyCode(user.getId(), code);
        return true;
    }

    @Transactional
    public boolean resetPassword(@NotBlank String email, @NotBlank String code, @NotBlank String newPassword) {
        User user = userReadService.findUserByEmail(email);
        userStatusService.validateUserStatus(user);

        passwordCodeService.verifyCode(user.getId(), code);
        passwordCodeService.deleteCode(user.getId());
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userWriteService.saveUser(user);

        return true;
    }
}
