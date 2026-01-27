package com.beta.controller.account.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTokenUpdateResponse {
    private boolean success;
    private String message;

    public static FcmTokenUpdateResponse success() {
        return FcmTokenUpdateResponse.builder()
                .success(true)
                .message("FCM 토큰이 정상적으로 업데이트되었습니다.")
                .build();
    }
}
