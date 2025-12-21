package com.beta.account.application.dto;

import com.beta.account.domain.entity.UserDevice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DeviceDto {

    private Long id;
    private String deviceId;
    private Long userId;
    private String fcmToken;
    private LocalDateTime lastUsedAt;
    private Boolean isActive;

    public static DeviceDto fromEntity(UserDevice device) {
        return DeviceDto.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .userId(device.getUserId())
                .fcmToken(device.getFcmToken())
                .lastUsedAt(device.getLastUsedAt())
                .isActive(device.getIsActive())
                .build();
    }

    public static List<DeviceDto> fromEntityList(List<UserDevice> devices) {
        return devices.stream()
                .map(DeviceDto::fromEntity)
                .collect(Collectors.toList());
    }
}
