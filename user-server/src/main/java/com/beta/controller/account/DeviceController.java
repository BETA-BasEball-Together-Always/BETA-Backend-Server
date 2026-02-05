package com.beta.controller.account;

import com.beta.account.application.DeviceAppService;
import com.beta.controller.account.request.PushEnabledRequest;
import com.beta.controller.account.request.PushSettingsRequest;
import com.beta.controller.account.response.PushSettingsResponse;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    @Operation(summary = "푸시 알림 설정 (FCM 토큰 + 푸시 활성화)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @PutMapping("/push-settings")
    public ResponseEntity<PushSettingsResponse> updatePushSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PushSettingsRequest request) {

        deviceAppService.updatePushSettings(
                userDetails.userId(),
                request.getDeviceId(),
                request.getFcmToken(),
                request.getPushEnabled()
        );

        return ResponseEntity.ok(PushSettingsResponse.success("푸시 알림 설정이 업데이트되었습니다."));
    }

    @Operation(summary = "푸시 알림 활성화/비활성화 토글")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @PatchMapping("/push-enabled")
    public ResponseEntity<PushSettingsResponse> updatePushEnabled(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PushEnabledRequest request) {

        deviceAppService.updatePushEnabled(
                userDetails.userId(),
                request.getDeviceId(),
                request.getPushEnabled()
        );

        return ResponseEntity.ok(PushSettingsResponse.success("푸시 알림 설정이 변경되었습니다."));
    }
}
