package com.beta.account.domain.service;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.User;
import com.beta.account.domain.entity.UserConsents;
import com.beta.account.infra.repository.UserConsentJpaRepository;
import com.beta.account.infra.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .password("encodedPassword")
                .nickname("testNickname")
                .socialProvider(SocialProvider.EMAIL)
                .baseballTeam(team)
                .build();

        User savedUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testNickname")
                .socialProvider(SocialProvider.EMAIL)
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
    @DisplayName("사용자 동의 정보를 저장한다")
    void saveAgreements_Success() {
        // given
        Long userId = 1L;
        Boolean agreeMarketing = true;
        Boolean personalInfoRequired = true;

        ArgumentCaptor<UserConsents> captor = ArgumentCaptor.forClass(UserConsents.class);
        when(userConsentJpaRepository.save(any(UserConsents.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userWriteService.saveAgreements(agreeMarketing, personalInfoRequired, userId);

        // then
        verify(userConsentJpaRepository, times(1)).save(captor.capture());
        UserConsents savedConsent = captor.getValue();

        assertThat(savedConsent.getUserId()).isEqualTo(userId);
        assertThat(savedConsent.getAgreeMarketing()).isTrue();
        assertThat(savedConsent.getPersonalInfoRequired()).isTrue();
    }

    @Test
    @DisplayName("마케팅 동의가 false인 경우 동의 정보를 저장한다")
    void saveAgreements_WithMarketingFalse() {
        // given
        Long userId = 2L;
        Boolean agreeMarketing = false;
        Boolean personalInfoRequired = true;

        ArgumentCaptor<UserConsents> captor = ArgumentCaptor.forClass(UserConsents.class);
        when(userConsentJpaRepository.save(any(UserConsents.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userWriteService.saveAgreements(agreeMarketing, personalInfoRequired, userId);

        // then
        verify(userConsentJpaRepository, times(1)).save(captor.capture());
        UserConsents savedConsent = captor.getValue();

        assertThat(savedConsent.getUserId()).isEqualTo(userId);
        assertThat(savedConsent.getAgreeMarketing()).isFalse();
        assertThat(savedConsent.getPersonalInfoRequired()).isTrue();
    }

    private BaseballTeam createBaseballTeam(String code, String name) {
        return BaseballTeam.builder()
                .code(code)
                .teamNameKr(name)
                .build();
    }
}
