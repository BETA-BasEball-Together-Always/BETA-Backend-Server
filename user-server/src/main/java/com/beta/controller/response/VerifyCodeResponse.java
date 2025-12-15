package com.beta.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyCodeResponse {
    private boolean success;
    private String message;

    public static VerifyCodeResponse success(boolean isSuccess) {
        return VerifyCodeResponse.builder()
                .success(isSuccess)
                .message("인증코드가 확인되었습니다.")
                .build();
    }
}
