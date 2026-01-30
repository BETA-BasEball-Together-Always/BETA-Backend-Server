package com.beta.controller.account;

import com.beta.account.application.DeviceAppService;
import com.beta.account.application.dto.DeviceRegisterResult;
import com.beta.controller.account.request.DeviceRegisterRequest;
import com.beta.controller.account.response.DeviceRegisterResponse;
import com.beta.controller.account.response.FcmTokenUpdateResponse;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Device", description = "디바이스 관련 API")
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceAppService deviceAppService;

    @Operation(summary = "디바이스 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT001, JWT002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "FCM 토큰 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT001, JWT002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
