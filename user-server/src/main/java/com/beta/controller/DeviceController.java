package com.beta.controller;

import com.beta.account.application.DeviceAppService;
import com.beta.account.application.dto.DeviceRegisterResult;
import com.beta.controller.request.DeviceRegisterRequest;
import com.beta.controller.response.DeviceRegisterResponse;
import com.beta.controller.response.FcmTokenUpdateResponse;
import com.beta.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceAppService deviceAppService;

    @PostMapping("/register")
    public ResponseEntity<DeviceRegisterResponse> registerDevice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeviceRegisterRequest request) {

        DeviceRegisterResult result = deviceAppService.registerOrUpdateDevice(
                userDetails.userId(),
                request.getDeviceId(),
                request.getFcmToken()
        );

        return ResponseEntity.ok(DeviceRegisterResponse.of(result));
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<FcmTokenUpdateResponse> updateFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeviceRegisterRequest request) {

        deviceAppService.updateFcmToken(
                userDetails.userId(),
                request.getDeviceId(),
                request.getFcmToken()
        );

        return ResponseEntity.ok(FcmTokenUpdateResponse.success());
    }
}
