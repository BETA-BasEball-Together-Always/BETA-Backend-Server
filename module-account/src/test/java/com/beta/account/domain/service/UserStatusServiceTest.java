package com.beta.account.domain.service;

import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.core.exception.ErrorCode;
import com.beta.core.exception.account.EmailDuplicateException;
import com.beta.core.exception.account.InvalidPasswordException;
import com.beta.core.exception.account.NameDuplicateException;
import com.beta.core.exception.account.PersonalInfoAgreementRequiredException;
import com.beta.core.exception.account.UserSuspendedException;
import com.beta.core.exception.account.UserWithdrawnException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserStatusService 단위 테스트")
class UserStatusServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserStatusService userStatusService;

    @Test
    @DisplayName("정상 상태의 사용자는 검증을 통과한다")
    void validateUserStatus_Success_WhenUserIsActive() {
        // given
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(User.UserStatus.ACTIVE);

        // when & then
        assertDoesNotThrow(() -> userStatusService.validateUserStatus(user));
    }

    @Test
    @DisplayName("탈퇴한 사용자는 UserWithdrawnException을 발생시킨다")
    void validateUserStatus_ThrowsUserWithdrawnException_WhenUserIsWithdrawn() {
        // given
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(User.UserStatus.WITHDRAWN);

        // when & then
        assertThatThrownBy(() -> userStatusService.validateUserStatus(user))
                .isInstanceOf(UserWithdrawnException.class)
                .hasMessage("탈퇴한 사용자입니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
    }

    @Test
    @DisplayName("정지된 사용자는 UserSuspendedException을 발생시킨다")
    void validateUserStatus_ThrowsUserSuspendedException_WhenUserIsSuspended() {
        // given
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(User.UserStatus.SUSPENDED);

        // when & then
        assertThatThrownBy(() -> userStatusService.validateUserStatus(user))
                .isInstanceOf(UserSuspendedException.class)
                .hasMessage("정지된 사용자입니다. 관리자에게 문의 하세요.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_SUSPENDED);
    }

    @Test
    @DisplayName("개인정보 수집 동의가 true이면 검증을 통과한다")
    void validateAgreePersonalInfo_Success_WhenAgreed() {
        // when & then
        assertDoesNotThrow(() -> userStatusService.validateAgreePersonalInfo(true));
    }

    @Test
    @DisplayName("개인정보 수집 동의가 false이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void validateAgreePersonalInfo_ThrowsException_WhenNotAgreed() {
        // when & then
        assertThatThrownBy(() -> userStatusService.validateAgreePersonalInfo(false))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED);
    }

    @Test
    @DisplayName("개인정보 수집 동의가 null이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void validateAgreePersonalInfo_ThrowsException_WhenNull() {
        // when & then
        assertThatThrownBy(() -> userStatusService.validateAgreePersonalInfo(null))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED);
    }

    @Test
    @DisplayName("닉네임이 중복되지 않으면 검증을 통과한다")
    void validateNameDuplicate_Success_WhenNicknameIsUnique() {
        // given
        String nickname = "uniqueNickname";
        when(userJpaRepository.existsByNickname(nickname)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> userStatusService.validateNameDuplicate(nickname));
        verify(userJpaRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("닉네임이 중복되면 NameDuplicateException을 발생시킨다")
    void validateNameDuplicate_ThrowsException_WhenNicknameIsDuplicated() {
        // given
        String nickname = "duplicateNickname";
        when(userJpaRepository.existsByNickname(nickname)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userStatusService.validateNameDuplicate(nickname))
                .isInstanceOf(NameDuplicateException.class)
                .hasMessage("이미 존재하는 닉네임입니다")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NAME_DUPLICATE);

        verify(userJpaRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("이메일이 중복되지 않으면 검증을 통과한다")
    void validateEmailDuplicate_Success_WhenEmailIsUnique() {
        // given
        String email = "unique@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> userStatusService.validateEmailDuplicate(email));
        verify(userJpaRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("이메일이 중복되면 EmailDuplicateException을 발생시킨다")
    void validateEmailDuplicate_ThrowsException_WhenEmailIsDuplicated() {
        // given
        String email = "duplicate@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userStatusService.validateEmailDuplicate(email))
                .isInstanceOf(EmailDuplicateException.class)
                .hasMessage("이미 존재하는 이메일입니다")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_DUPLICATE);

        verify(userJpaRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("닉네임이 중복되지 않으면 false를 반환한다")
    void isNameDuplicate_ReturnsFalse_WhenNicknameIsUnique() {
        // given
        String nickname = "uniqueNickname";
        when(userJpaRepository.existsByNickname(nickname)).thenReturn(false);

        // when
        boolean result = userStatusService.isNameDuplicate(nickname);

        // then
        assertThat(result).isFalse();
        verify(userJpaRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("닉네임이 중복되면 true를 반환한다")
    void isNameDuplicate_ReturnsTrue_WhenNicknameIsDuplicated() {
        // given
        String nickname = "duplicateNickname";
        when(userJpaRepository.existsByNickname(nickname)).thenReturn(true);

        // when
        boolean result = userStatusService.isNameDuplicate(nickname);

        // then
        assertThat(result).isTrue();
        verify(userJpaRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("이메일이 중복되지 않으면 false를 반환한다")
    void isEmailDuplicate_ReturnsFalse_WhenEmailIsUnique() {
        // given
        String email = "unique@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(false);

        // when
        boolean result = userStatusService.isEmailDuplicate(email);

        // then
        assertThat(result).isFalse();
        verify(userJpaRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("이메일이 중복되면 true를 반환한다")
    void isEmailDuplicate_ReturnsTrue_WhenEmailIsDuplicated() {
        // given
        String email = "duplicate@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userStatusService.isEmailDuplicate(email);

        // then
        assertThat(result).isTrue();
        verify(userJpaRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("비밀번호가 일치하면 검증을 통과한다")
    void validatePasswordExistence_Success_WhenPasswordMatches() {
        // given
        String encodedPassword = "encodedPassword123";
        String rawPassword = "rawPassword123";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> userStatusService.validatePasswordExistence(encodedPassword, rawPassword));
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 InvalidPasswordException을 발생시킨다")
    void validatePasswordExistence_ThrowsException_WhenPasswordDoesNotMatch() {
        // given
        String encodedPassword = "encodedPassword123";
        String rawPassword = "wrongPassword";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userStatusService.validatePasswordExistence(encodedPassword, rawPassword))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PASSWORD);

        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
}
