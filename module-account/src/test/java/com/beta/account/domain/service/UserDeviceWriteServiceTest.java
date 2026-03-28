package com.beta.account.domain.service;

import com.beta.account.domain.entity.UserDevice;
import com.beta.account.infra.repository.UserDeviceJpaRepository;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("특정 디바이스를 비활성화한다")
    void deactivateByDeviceId_DeactivatesSpecificDevice_WhenDeviceExists() {
        // given
        Long userId = 1L;
        String deviceId = "device-uuid-123";
        UserDevice device = createUserDevice(userId, "token123");

        when(userDeviceJpaRepository.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.of(device));
        when(userDeviceJpaRepository.save(any(UserDevice.class)))
                .thenReturn(device);

        // when
        userDeviceWriteService.deactivateByDeviceId(userId, deviceId);

        // then
        assertThat(device.getIsActive()).isFalse();
        verify(userDeviceJpaRepository, times(1)).findByUserIdAndDeviceId(userId, deviceId);
        verify(userDeviceJpaRepository, times(1)).save(device);
    }

    @Test
    @DisplayName("비활성화 대상 디바이스가 없으면 저장하지 않는다")
    void deactivateByDeviceId_DoesNothing_WhenDeviceDoesNotExist() {
        // given
        Long userId = 1L;
        String deviceId = "device-uuid-123";

        when(userDeviceJpaRepository.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.empty());

        // when
        userDeviceWriteService.deactivateByDeviceId(userId, deviceId);

        // then
        verify(userDeviceJpaRepository, times(1)).findByUserIdAndDeviceId(userId, deviceId);
        verify(userDeviceJpaRepository, never()).save(any(UserDevice.class));
    }

    @Test
    @DisplayName("디바이스 FCM 토큰을 업데이트한다")
    void updateDeviceFcmToken_UpdatesFcmToken() {
        // given
        UserDevice device = createUserDevice(1L, "old-token");
        String newFcmToken = "new-fcm-token";

        when(userDeviceJpaRepository.save(any(UserDevice.class)))
                .thenReturn(device);

        // when
        userDeviceWriteService.updateDeviceFcmToken(device, newFcmToken);

        // then
        assertThat(device.getFcmToken()).isEqualTo(newFcmToken);
        assertThat(device.getLastUsedAt()).isNotNull();

        verify(userDeviceJpaRepository, times(1)).save(device);
    }

    @Test
    @DisplayName("디바이스 전체 푸시 활성화 상태를 변경하면 세부 토글도 함께 변경한다")
    void updatePushEnabled_UpdatesOverallAndDetailSettings() {
        // given
        UserDevice device = createUserDevice(1L, "token123");
        device.updatePushDetailSettings(true, false);
        Boolean pushEnabled = false;

        when(userDeviceJpaRepository.save(any(UserDevice.class)))
                .thenReturn(device);

        // when
        userDeviceWriteService.updatePushEnabled(device, pushEnabled);

        // then
        assertThat(device.getPushEnabled()).isFalse();
        assertThat(device.getPostCommentPushEnabled()).isFalse();
        assertThat(device.getPostEmotionPushEnabled()).isFalse();

        verify(userDeviceJpaRepository, times(1)).save(device);
    }

    @Test
    @DisplayName("디바이스 푸시 세부 설정만 변경한다")
    void updatePushDetailSettings_UpdatesDetailSettings() {
        // given
        UserDevice device = createUserDevice(1L, "token123");

        when(userDeviceJpaRepository.save(any(UserDevice.class)))
                .thenReturn(device);

        // when
        userDeviceWriteService.updatePushDetailSettings(device, true, false);

        // then
        assertThat(device.getPostCommentPushEnabled()).isTrue();
        assertThat(device.getPostEmotionPushEnabled()).isFalse();
        assertThat(device.getPushEnabled()).isFalse();
        verify(userDeviceJpaRepository, times(1)).save(device);
    }

    @Test
    @DisplayName("디바이스 푸시 세부 설정이 모두 활성화되면 전체 상태도 활성화된다")
    void updatePushDetailSettings_UpdatesOverallState_WhenAllDetailSettingsEnabled() {
        // given
        UserDevice device = createUserDevice(1L, "token123");

        when(userDeviceJpaRepository.save(any(UserDevice.class)))
                .thenReturn(device);

        // when
        userDeviceWriteService.updatePushDetailSettings(device, true, true);

        // then
        assertThat(device.getPostCommentPushEnabled()).isTrue();
        assertThat(device.getPostEmotionPushEnabled()).isTrue();
        assertThat(device.getPushEnabled()).isTrue();
        verify(userDeviceJpaRepository, times(1)).save(device);
    }

    private UserDevice createUserDevice(Long userId, String fcmToken) {
        return UserDevice.builder()
                .userId(userId)
                .deviceId("device-uuid-123")
                .fcmToken(fcmToken)
                .build();
    }
}
