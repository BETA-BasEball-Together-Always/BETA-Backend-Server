package com.beta.account.application;

import com.beta.account.domain.service.UserDeviceReadService;
import com.beta.account.domain.service.UserDeviceWriteService;
import com.beta.core.exception.account.DeviceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceAppService 단위 테스트")
class DeviceAppServiceTest {

    @Mock
    UserDeviceReadService userDeviceReadService;

    @Mock
    UserDeviceWriteService userDeviceWriteService;

    @InjectMocks
    DeviceAppService deviceAppService;

    @Test
    @DisplayName("푸시 설정 업데이트 시 디바이스가 없으면 DeviceNotFoundException이 발생한다")
    void update_push_settings_throws_device_not_found_exception_when_device_missing() {
        // given
        Long userId = 1L;
        String deviceId = "device-1";
        when(userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> deviceAppService.updatePushSettings(userId, deviceId, "fcm-token"))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    @DisplayName("푸시 토글 업데이트 시 디바이스가 없으면 DeviceNotFoundException이 발생한다")
    void update_push_enabled_throws_device_not_found_exception_when_device_missing() {
        // given
        Long userId = 1L;
        String deviceId = "device-1";
        when(userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> deviceAppService.updatePushEnabled(userId, deviceId, true))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    @DisplayName("푸시 토큰 업데이트 시 디바이스 FCM 토큰만 갱신한다")
    void update_push_settings_updates_fcm_token_only() {
        // given
        Long userId = 1L;
        String deviceId = "device-1";
        var device = com.beta.account.domain.entity.UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .fcmToken(null)
                .build();

        when(userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.of(device));

        // when
        deviceAppService.updatePushSettings(userId, deviceId, "fcm-token");

        // then
        verify(userDeviceWriteService).updateDeviceFcmToken(eq(device), eq("fcm-token"));
    }

    @Test
    @DisplayName("푸시 세부 설정 업데이트 시 디바이스가 없으면 DeviceNotFoundException이 발생한다")
    void update_push_detail_settings_throws_device_not_found_exception_when_device_missing() {
        // given
        Long userId = 1L;
        String deviceId = "device-1";
        when(userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> deviceAppService.updatePushDetailSettings(userId, deviceId, true, false))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    @DisplayName("푸시 세부 설정 업데이트 시 세부 토글 값만 전달한다")
    void update_push_detail_settings_updates_detail_values_only() {
        // given
        Long userId = 1L;
        String deviceId = "device-1";
        var device = com.beta.account.domain.entity.UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .fcmToken(null)
                .build();

        when(userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId))
                .thenReturn(Optional.of(device));

        // when
        deviceAppService.updatePushDetailSettings(userId, deviceId, true, false);

        // then
        verify(userDeviceWriteService).updatePushDetailSettings(eq(device), eq(true), eq(false));
    }
}
