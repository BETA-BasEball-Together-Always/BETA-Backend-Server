package com.beta.account.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceRegisterResult {

    private Long id;
    private String deviceId;
    private boolean isNewDevice;

    public static DeviceRegisterResult of(Long id, String deviceId, boolean isNewDevice) {
        return DeviceRegisterResult.builder()
                .id(id)
                .deviceId(deviceId)
                .isNewDevice(isNewDevice)
                .build();
    }
}
