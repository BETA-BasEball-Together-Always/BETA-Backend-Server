package com.beta.account.application;

import com.beta.account.application.dto.DevicePushSettingsResult;
import com.beta.account.domain.entity.UserDevice;
import com.beta.account.domain.service.UserDeviceReadService;
import com.beta.account.domain.service.UserDeviceWriteService;
import com.beta.core.exception.account.DeviceNotFoundException;
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
        deactivateOtherUsersDevices(deviceId, userId);

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

    private void deactivateOtherUsersDevices(String deviceId, Long currentUserId) {
        userDeviceReadService.findActiveDevicesByDeviceIdExcludingUser(deviceId, currentUserId)
                .forEach(userDeviceWriteService::deactivateDevice);
    }

    @Transactional
    public void updatePushSettings(Long userId, String deviceId, String fcmToken) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(DeviceNotFoundException::new);

        userDeviceWriteService.updateDeviceFcmToken(device, fcmToken);
    }

    @Transactional
    public void updatePushEnabled(Long userId, String deviceId, Boolean pushEnabled) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(DeviceNotFoundException::new);

        userDeviceWriteService.updatePushEnabled(device, pushEnabled);
    }

    @Transactional
    public void updatePushDetailSettings(Long userId, String deviceId,
                                         Boolean postCommentPushEnabled, Boolean postEmotionPushEnabled) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(DeviceNotFoundException::new);

        userDeviceWriteService.updatePushDetailSettings(
                device,
                postCommentPushEnabled,
                postEmotionPushEnabled
        );
    }

    public DevicePushSettingsResult getPushSettings(Long userId, String deviceId) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(DeviceNotFoundException::new);

        return DevicePushSettingsResult.builder()
                .deviceId(device.getDeviceId())
                .pushEnabled(device.getPushEnabled())
                .postCommentPushEnabled(device.getPostCommentPushEnabled())
                .postEmotionPushEnabled(device.getPostEmotionPushEnabled())
                .build();
    }
}
