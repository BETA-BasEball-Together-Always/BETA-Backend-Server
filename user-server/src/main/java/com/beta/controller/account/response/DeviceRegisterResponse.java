package com.beta.controller.account.response;

import com.beta.account.application.dto.DeviceRegisterResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceRegisterResponse {

    private Long id;
    private String deviceId;
    private String message;

    public static DeviceRegisterResponse of(DeviceRegisterResult result) {
        String message = result.isNewDevice()
                ? "디바이스가 성공적으로 등록되었습니다"
                : "디바이스 정보가 업데이트되었습니다";

        return DeviceRegisterResponse.builder()
                .id(result.getId())
                .deviceId(result.getDeviceId())
                .message(message)
                .build();
    }
}
