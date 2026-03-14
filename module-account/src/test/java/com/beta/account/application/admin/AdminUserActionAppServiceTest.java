package com.beta.account.application.admin;

import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.account.domain.service.UserStatusService;
import com.beta.account.domain.service.UserWriteService;
import com.beta.core.exception.account.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserActionAppService 단위 테스트")
class AdminUserActionAppServiceTest {

    @Mock
    UserReadService userReadService;

    @Mock
    UserStatusService userStatusService;

    @Mock
    UserWriteService userWriteService;

    @InjectMocks
    AdminUserActionAppService adminUserActionAppService;

    @Test
    @DisplayName("회원 정지 시 조회, 검증, 상태 변경을 순서대로 수행한다")
    void suspend_member_calls_read_validate_and_write() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        when(userReadService.findUserById(userId)).thenReturn(user);

        // when
        adminUserActionAppService.suspendUser(userId);

        // then
        verify(userReadService).findUserById(userId);
        verify(userStatusService).validateSuspend(user);
        verify(userWriteService).suspend(user);
    }

    @Test
    @DisplayName("회원 정지 해제 시 조회, 검증, 상태 변경을 순서대로 수행한다")
    void unsuspend_member_calls_read_validate_and_write() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        when(userReadService.findUserById(userId)).thenReturn(user);

        // when
        adminUserActionAppService.unsuspendUser(userId);

        // then
        verify(userReadService).findUserById(userId);
        verify(userStatusService).validateUnsuspend(user);
        verify(userWriteService).unsuspend(user);
    }

    @Test
    @DisplayName("관리 대상 사용자가 없으면 사용자 조회 예외가 발생한다")
    void suspend_member_throws_user_not_found_exception() {
        // given
        Long userId = 1L;
        when(userReadService.findUserById(userId))
                .thenThrow(new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        // when // then
        assertThatThrownBy(() -> adminUserActionAppService.suspendUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
