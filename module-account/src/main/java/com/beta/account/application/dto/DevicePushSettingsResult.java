package com.beta.account.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DevicePushSettingsResult {

    private String deviceId;
    private Boolean pushEnabled;
    private Boolean postCommentPushEnabled;
    private Boolean postEmotionPushEnabled;
}
