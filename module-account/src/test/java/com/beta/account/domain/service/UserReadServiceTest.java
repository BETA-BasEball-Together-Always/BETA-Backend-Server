package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private BaseballTeam createBaseballTeam(String code, String name) {
        return BaseballTeam.builder()
                .code(code)
                .teamNameKr(name)
                .build();
    }

    private User createUser(Long id, String email, String nickname, String socialId, SocialProvider provider, BaseballTeam team) {
        return User.builder()
                .email(email)
                .password("encodedPassword")
                .nickname(nickname)
                .socialId(socialId)
                .socialProvider(provider)
                .baseballTeam(team)
                .build();
    }
}
