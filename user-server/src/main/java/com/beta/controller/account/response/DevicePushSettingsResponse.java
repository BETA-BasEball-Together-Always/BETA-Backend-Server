package com.beta.controller.account.response;

import com.beta.account.application.dto.DevicePushSettingsResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "디바이스 푸시 설정 조회 응답")
public class DevicePushSettingsResponse {

    @Schema(description = "디바이스 식별자", example = "FE053853-B36D-49B8-B52A-706CB7BDAFB1")
    private String deviceId;

    @Schema(description = "전체 푸시 토글 상태", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "댓글 알림 토글 상태", example = "true")
    private Boolean postCommentPushEnabled;

    @Schema(description = "공감 알림 토글 상태", example = "false")
    private Boolean postEmotionPushEnabled;

    public static DevicePushSettingsResponse from(DevicePushSettingsResult result) {
        return DevicePushSettingsResponse.builder()
                .deviceId(result.getDeviceId())
                .pushEnabled(result.getPushEnabled())
                .postCommentPushEnabled(result.getPostCommentPushEnabled())
                .postEmotionPushEnabled(result.getPostEmotionPushEnabled())
                .build();
    }
}
