package com.beta.account.domain.service;

import com.beta.account.domain.entity.User;
import com.beta.core.exception.ErrorCode;
import com.beta.core.exception.account.NameDuplicateException;
import com.beta.core.exception.account.PersonalInfoAgreementRequiredException;
import com.beta.core.exception.account.UserSuspendedException;
import com.beta.core.exception.account.UserWithdrawnException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialUserStatusService 단위 테스트")
class SocialUserStatusServiceTest {

    @InjectMocks
    private SocialUserStatusService socialUserStatusService;

    @Test
    @DisplayName("정상 상태의 사용자는 검증을 통과한다")
    void validateUserStatus_Success_WhenUserIsActive() {
        // given
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(User.UserStatus.ACTIVE);

        // when & then
        assertDoesNotThrow(() -> socialUserStatusService.validateUserStatus(user));
    }

    @Test
    @DisplayName("탈퇴한 사용자는 UserWithdrawnException을 발생시킨다")
    void validateUserStatus_ThrowsUserWithdrawnException_WhenUserIsWithdrawn() {
        // given
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(User.UserStatus.WITHDRAWN);

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateUserStatus(user))
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
        assertThatThrownBy(() -> socialUserStatusService.validateUserStatus(user))
                .isInstanceOf(UserSuspendedException.class)
                .hasMessage("정지된 사용자입니다. 관리자에게 문의 하세요.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_SUSPENDED);
    }

    @Test
    @DisplayName("개인정보 수집 동의가 true이면 검증을 통과한다")
    void validateAgreePersonalInfo_Success_WhenAgreed() {
        // when & then
        assertDoesNotThrow(() -> socialUserStatusService.validateAgreePersonalInfo(true));
    }

    @Test
    @DisplayName("개인정보 수집 동의가 false이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void validateAgreePersonalInfo_ThrowsException_WhenNotAgreed() {
        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateAgreePersonalInfo(false))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED);
    }

    @Test
    @DisplayName("개인정보 수집 동의가 null이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void validateAgreePersonalInfo_ThrowsException_WhenNull() {
        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateAgreePersonalInfo(null))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED);
    }

    @Test
    @DisplayName("이름이 중복되지 않으면 검증을 통과한다")
    void validateNameDuplicate_Success_WhenNameIsUnique() {
        // when & then
        assertDoesNotThrow(() -> socialUserStatusService.validateNameDuplicate(false));
    }

    @Test
    @DisplayName("이름이 중복되면 NameDuplicateException을 발생시킨다")
    void validateNameDuplicate_ThrowsException_WhenNameIsDuplicated() {
        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateNameDuplicate(true))
                .isInstanceOf(NameDuplicateException.class)
                .hasMessage("이미 존재하는 이름입니다.")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NAME_DUPLICATE);
    }
}
