package com.beta.controller.account.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PushDetailSettingsRequest {

    @NotBlank(message = "디바이스 ID는 필수입니다")
    private String deviceId;

    @NotNull(message = "댓글 알림 설정은 필수입니다")
    private Boolean postCommentPushEnabled;

    @NotNull(message = "공감 알림 설정은 필수입니다")
    private Boolean postEmotionPushEnabled;
}
