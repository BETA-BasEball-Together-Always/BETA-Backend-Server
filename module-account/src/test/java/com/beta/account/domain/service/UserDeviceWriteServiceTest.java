package com.beta.account.domain.service;

import com.beta.account.domain.entity.UserDevice;
import com.beta.account.infra.repository.UserDeviceJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDeviceWriteService 단위 테스트")
class UserDeviceWriteServiceTest {

    @Mock
    private UserDeviceJpaRepository userDeviceJpaRepository;

    @InjectMocks
    private UserDeviceWriteService userDeviceWriteService;

    @Test
    @DisplayName("디바이스를 저장한다")
    void save_ReturnsDevice_WhenDeviceIsSaved() {
        // given
        UserDevice device = createUserDevice(1L, "token123");
        UserDevice savedDevice = createUserDevice(1L, "token123");

        when(userDeviceJpaRepository.save(any(UserDevice.class)))
                .thenReturn(savedDevice);

        // when
        UserDevice result = userDeviceWriteService.save(device);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFcmToken()).isEqualTo("token123");

        verify(userDeviceJpaRepository, times(1)).save(device);
    }

    @Test
    @DisplayName("사용자의 모든 디바이스를 삭제한다")
    void deleteAllByUserId_DeletesAllDevices_WhenUserIdProvided() {
        // given
        Long userId = 1L;

        doNothing().when(userDeviceJpaRepository).deleteAllByUserId(userId);

        // when
        userDeviceWriteService.deleteAllByUserId(userId);

        // then
        verify(userDeviceJpaRepository, times(1)).deleteAllByUserId(userId);
    }

    @Test
    @DisplayName("특정 디바이스만 삭제한다")
    void deleteByDeviceId_DeletesSpecificDevice_WhenDeviceIdProvided() {
        // given
        Long userId = 1L;
        String deviceId = "device-uuid-123";

        doNothing().when(userDeviceJpaRepository).deleteByUserIdAndDeviceId(userId, deviceId);

        // when
        userDeviceWriteService.deleteByDeviceId(userId, deviceId);

        // then
        verify(userDeviceJpaRepository, times(1)).deleteByUserIdAndDeviceId(userId, deviceId);
    }

    private UserDevice createUserDevice(Long userId, String fcmToken) {
        return UserDevice.builder()
                .userId(userId)
                .deviceId("device-uuid-123")
                .fcmToken(fcmToken)
                .build();
    }
}
