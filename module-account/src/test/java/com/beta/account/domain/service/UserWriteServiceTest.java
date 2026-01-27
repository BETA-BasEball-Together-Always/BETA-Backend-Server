package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.SignupStep;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.entity.UserConsents;
import com.beta.account.infra.repository.UserConsentJpaRepository;
import com.beta.account.infra.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserWriteService 단위 테스트")
class UserWriteServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserConsentJpaRepository userConsentJpaRepository;

    @InjectMocks
    private UserWriteService userWriteService;

    @Test
    @DisplayName("사용자를 저장하고 UserDto로 반환한다")
    void saveUser_Success() {
        // given
        BaseballTeam team = createBaseballTeam("SSG", "SSG 랜더스");
        User user = User.builder()
                .email("test@example.com")
                .nickname("testNickname")
                .socialProvider(SocialProvider.KAKAO)
                .baseballTeam(team)
                .build();

        User savedUser = User.builder()
                .email("test@example.com")
                .nickname("testNickname")
                .socialProvider(SocialProvider.KAKAO)
                .baseballTeam(team)
                .build();

        when(userJpaRepository.save(user)).thenReturn(savedUser);

        // when
        UserDto result = userWriteService.saveUser(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testNickname");
        verify(userJpaRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("소셜 사용자를 생성한다")
    void createSocialUser_Success() {
        // given
        String socialId = "apple_123";
        SocialProvider provider = SocialProvider.APPLE;
        User newUser = User.createNewSocialUser(socialId, provider);

        when(userJpaRepository.save(any(User.class))).thenReturn(newUser);

        // when
        User result = userWriteService.createSocialUser(socialId, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSocialId()).isEqualTo(socialId);
        assertThat(result.getSocialProvider()).isEqualTo(provider);
        assertThat(result.getSignupStep()).isEqualTo(SignupStep.SOCIAL_AUTHENTICATED);
        verify(userJpaRepository).save(any(User.class));
    }

    @Nested
    @DisplayName("processConsent 테스트")
    class ProcessConsentTest {

        @Test
        @DisplayName("개인정보 동의 처리가 성공한다")
        void processConsent_Success() {
            // given
            Long userId = 1L;
            User user = mock(User.class);

            when(userJpaRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

            // when
            User result = userWriteService.processConsent(userId, false, true);

            // then
            assertThat(result).isEqualTo(user);
            verify(user).agreeConsent();
            verify(userConsentJpaRepository).save(any(UserConsents.class));
        }
    }

    @Nested
    @DisplayName("updateProfile 테스트")
    class UpdateProfileTest {

        @Test
        @DisplayName("프로필 업데이트가 성공한다")
        void updateProfile_Success() {
            // given
            Long userId = 1L;
            String email = "test@example.com";
            String nickname = "테스터";
            User user = mock(User.class);

            when(userJpaRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

            // when
            User result = userWriteService.updateProfile(userId, email, nickname);

            // then
            assertThat(result).isEqualTo(user);
            verify(user).updateProfile(email, nickname);
        }
    }

    @Nested
    @DisplayName("updateTeam 테스트")
    class UpdateTeamTest {

        @Test
        @DisplayName("팀 선택이 성공한다")
        void updateTeam_Success() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            BaseballTeam team = createBaseballTeam("LG", "LG 트윈스");

            when(userJpaRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

            // when
            User result = userWriteService.updateTeam(userId, team);

            // then
            assertThat(result).isEqualTo(user);
            verify(user).updateTeam(team);
        }
    }

    @Nested
    @DisplayName("completeSignup 테스트")
    class CompleteSignupTest {

        @Test
        @DisplayName("회원가입 완료(건너뛰기)가 성공한다")
        void completeSignup_Success() {
            // given
            Long userId = 1L;
            User user = mock(User.class);

            when(userJpaRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

            // when
            User result = userWriteService.completeSignup(userId);

            // then
            assertThat(result).isEqualTo(user);
            verify(user).completeSignup();
        }
    }

    @Nested
    @DisplayName("completeSignupWithInfo 테스트")
    class CompleteSignupWithInfoTest {

        @Test
        @DisplayName("선택정보 입력 후 회원가입 완료가 성공한다")
        void completeSignupWithInfo_Success() {
            // given
            Long userId = 1L;
            User user = mock(User.class);

            when(userJpaRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

            // when
            User result = userWriteService.completeSignupWithInfo(userId, User.GenderType.M, 25);

            // then
            assertThat(result).isEqualTo(user);
            verify(user).updateOptionalInfo(User.GenderType.M, 25);
            verify(user).completeSignup();
        }

        @Test
        @DisplayName("성별 없이 나이만 입력해도 성공한다")
        void completeSignupWithInfo_NullGender() {
            // given
            Long userId = 1L;
            User user = mock(User.class);

            when(userJpaRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

            // when
            User result = userWriteService.completeSignupWithInfo(userId, null, 30);

            // then
            verify(user).updateOptionalInfo(null, 30);
            verify(user).completeSignup();
        }
    }

    private BaseballTeam createBaseballTeam(String code, String name) {
        return BaseballTeam.builder()
                .code(code)
                .teamNameKr(name)
                .build();
    }
}
