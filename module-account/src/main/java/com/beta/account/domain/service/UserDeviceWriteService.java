package com.beta.account.domain.service;

import com.beta.account.domain.entity.UserDevice;
import com.beta.account.infra.repository.UserDeviceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDeviceWriteService {

    private final UserDeviceJpaRepository userDeviceJpaRepository;

    public UserDevice save(UserDevice userDevice) {
        return userDeviceJpaRepository.save(userDevice);
    }

    public void deleteAllByUserId(Long userId) {
        userDeviceJpaRepository.deleteAllByUserId(userId);
    }

    public void deleteByDeviceId(Long userId, String deviceId) {
        userDeviceJpaRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }

    public UserDevice createNewDevice(Long userId, String deviceId, String fcmToken) {
        UserDevice newDevice = UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .fcmToken(fcmToken)
                .build();
        return save(newDevice);
    }

    public void updateExistingDevice(UserDevice device, String fcmToken) {
        if (fcmToken != null && !fcmToken.isBlank()) {
            device.updateFcmToken(fcmToken);
        }
        device.updateLastUsedAt();
        device.activate();
        save(device);
    }

    public void updateDeviceFcmToken(UserDevice device, String fcmToken) {
        device.updateFcmToken(fcmToken);
        device.updateLastUsedAt();
        save(device);
    }

    public void updatePushSettings(UserDevice device, String fcmToken, Boolean pushEnabled) {
        device.updatePushSettings(fcmToken, pushEnabled);
        device.updateLastUsedAt();
        save(device);
    }

    public void updatePushEnabled(UserDevice device, Boolean pushEnabled) {
        device.updatePushEnabled(pushEnabled);
        save(device);
    }
}
