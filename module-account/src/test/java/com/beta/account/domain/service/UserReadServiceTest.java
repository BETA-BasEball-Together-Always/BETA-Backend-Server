package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.core.exception.account.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserReadService 단위 테스트")
class UserReadServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserReadService userReadService;

    @Test
    @DisplayName("소셜 ID와 제공자로 사용자를 조회한다")
    void findUserBySocialId_ReturnsUser_WhenUserExists() {
        // given
        String socialId = "kakao-123456";
        SocialProvider provider = SocialProvider.KAKAO;
        BaseballTeam team = createBaseballTeam("KIA", "KIA 타이거즈");
        User expectedUser = createUser(1L, "user@example.com", "nickname", socialId, provider, team);

        when(userJpaRepository.findBySocialIdAndSocialProvider(socialId, provider))
                .thenReturn(Optional.of(expectedUser));

        // when
        User result = userReadService.findUserBySocialId(socialId, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSocialId()).isEqualTo(socialId);
        assertThat(result.getSocialProvider()).isEqualTo(provider);
        assertThat(result.getNickname()).isEqualTo("nickname");

        verify(userJpaRepository, times(1)).findBySocialIdAndSocialProvider(socialId, provider);
    }

    @Test
    @DisplayName("존재하지 않는 소셜 ID로 조회 시 null을 반환한다")
    void findUserBySocialId_ReturnsNull_WhenUserNotFound() {
        // given
        String socialId = "naver-999999";
        SocialProvider provider = SocialProvider.NAVER;

        when(userJpaRepository.findBySocialIdAndSocialProvider(socialId, provider))
                .thenReturn(Optional.empty());

        // when
        User result = userReadService.findUserBySocialId(socialId, provider);

        // then
        assertThat(result).isNull();
        verify(userJpaRepository, times(1)).findBySocialIdAndSocialProvider(socialId, provider);
    }

    @Test
    @DisplayName("다른 제공자의 동일한 소셜 ID는 다른 사용자로 간주된다")
    void findUserBySocialId_DifferentProviders() {
        // given
        String socialId = "same-social-id";
        SocialProvider kakaoProvider = SocialProvider.KAKAO;
        SocialProvider naverProvider = SocialProvider.NAVER;

        BaseballTeam team = createBaseballTeam("SSG", "SSG 랜더스");
        User kakaoUser = createUser(1L, "kakao@example.com", "kakaoNick", socialId, kakaoProvider, team);

        when(userJpaRepository.findBySocialIdAndSocialProvider(socialId, kakaoProvider))
                .thenReturn(Optional.of(kakaoUser));
        when(userJpaRepository.findBySocialIdAndSocialProvider(socialId, naverProvider))
                .thenReturn(Optional.empty());

        // when
        User kakaoResult = userReadService.findUserBySocialId(socialId, kakaoProvider);
        User naverResult = userReadService.findUserBySocialId(socialId, naverProvider);

        // then
        assertThat(kakaoResult).isNotNull();
        assertThat(kakaoResult.getSocialProvider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(naverResult).isNull();

        verify(userJpaRepository).findBySocialIdAndSocialProvider(socialId, kakaoProvider);
        verify(userJpaRepository).findBySocialIdAndSocialProvider(socialId, naverProvider);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findUserByEmail_ReturnsUser_WhenUserExists() {
        // given
        String email = "test@example.com";
        BaseballTeam team = createBaseballTeam("LG", "LG 트윈스");
        User expectedUser = createUser(1L, email, "testNick", null, SocialProvider.KAKAO, team);

        when(userJpaRepository.findByEmail(email))
                .thenReturn(Optional.of(expectedUser));

        // when
        User result = userReadService.findUserByEmail(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getNickname()).isEqualTo("testNick");

        verify(userJpaRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 UserNotFoundException을 발생시킨다")
    void findUserByEmail_ThrowsException_WhenUserNotFound() {
        // given
        String email = "notfound@example.com";

        when(userJpaRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userReadService.findUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("해당 이메일로 가입된 사용자가 없습니다.");

        verify(userJpaRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("ID로 사용자를 조회한다")
    void findUserById_ReturnsUser_WhenUserExists() {
        // given
        Long userId = 1L;
        BaseballTeam team = createBaseballTeam("DOOSAN", "두산 베어스");
        User expectedUser = createUser(userId, "user@example.com", "userNick", null, SocialProvider.NAVER, team);

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.of(expectedUser));

        // when
        User result = userReadService.findUserById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getNickname()).isEqualTo("userNick");

        verify(userJpaRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 UserNotFoundException을 발생시킨다")
    void findUserById_ThrowsException_WhenUserNotFound() {
        // given
        Long userId = 999L;

        when(userJpaRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userReadService.findUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("해당 ID의 사용자를 찾을 수 없습니다.");

        verify(userJpaRepository, times(1)).findById(userId);
    }

    private BaseballTeam createBaseballTeam(String code, String name) {
        return BaseballTeam.builder()
                .code(code)
                .teamNameKr(name)
                .build();
    }

    private User createUser(Long id, String email, String nickname, String socialId, SocialProvider provider, BaseballTeam team) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .socialId(socialId)
                .socialProvider(provider)
                .baseballTeam(team)
                .build();
    }
}
