package com.beta.controller.account.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceRegisterRequest {

    @NotBlank(message = "디바이스 ID는 필수입니다")
    private String deviceId;

    private String fcmToken;
}
