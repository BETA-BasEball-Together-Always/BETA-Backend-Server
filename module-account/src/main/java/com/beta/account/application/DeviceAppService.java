package com.beta.account.application;

import com.beta.account.domain.entity.UserDevice;
import com.beta.account.domain.service.UserDeviceReadService;
import com.beta.account.domain.service.UserDeviceWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceAppService {

    private final UserDeviceReadService userDeviceReadService;
    private final UserDeviceWriteService userDeviceWriteService;

    @Transactional
    public boolean registerOrUpdateDevice(Long userId, String deviceId, String fcmToken) {
        Optional<UserDevice> existingDevice = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId);

        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();
            userDeviceWriteService.updateExistingDevice(device, fcmToken);
            return false;
        } else {
            userDeviceWriteService.createNewDevice(userId, deviceId, fcmToken);
            return true;
        }
    }

    @Transactional
    public void updatePushSettings(Long userId, String deviceId, String fcmToken, Boolean pushEnabled) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다"));

        userDeviceWriteService.updatePushSettings(device, fcmToken, pushEnabled);
    }

    @Transactional
    public void updatePushEnabled(Long userId, String deviceId, Boolean pushEnabled) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다"));

        userDeviceWriteService.updatePushEnabled(device, pushEnabled);
    }
}
