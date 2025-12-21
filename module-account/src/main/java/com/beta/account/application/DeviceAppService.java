package com.beta.account.application;

import com.beta.account.application.dto.DeviceRegisterResult;
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

    /*====================DeviceController======================*/

    @Transactional
    public DeviceRegisterResult registerOrUpdateDevice(Long userId, String deviceId, String fcmToken) {
        Optional<UserDevice> existingDevice = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId);

        if (existingDevice.isPresent()) { // 기존 디바이스 업데이트
            UserDevice device = existingDevice.get();
            userDeviceWriteService.updateExistingDevice(device, fcmToken);
            return DeviceRegisterResult.of(device.getId(), deviceId, false);
        } else { // 새 디바이스 생성
            UserDevice newDevice = userDeviceWriteService.createNewDevice(userId, deviceId, fcmToken);
            return DeviceRegisterResult.of(newDevice.getId(), deviceId, true);
        }
    }

    @Transactional
    public void updateFcmToken(Long userId, String deviceId, String fcmToken) {
        UserDevice device = userDeviceReadService.findByUserIdAndDeviceId(userId, deviceId)
                .orElseThrow(() -> new IllegalArgumentException("디바이스를 찾을 수 없습니다"));

        userDeviceWriteService.updateDeviceFcmToken(device, fcmToken);
    }
}
