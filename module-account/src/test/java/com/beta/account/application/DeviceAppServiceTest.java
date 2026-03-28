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
        assertThatThrownBy(() -> deviceAppService.updatePushSettings(userId, deviceId, "fcm-token", true))
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
}
