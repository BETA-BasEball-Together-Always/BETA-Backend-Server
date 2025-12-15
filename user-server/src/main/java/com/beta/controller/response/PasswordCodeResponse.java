package com.beta.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordCodeResponse {
    private boolean success;
    private String message;

    public static PasswordCodeResponse success(boolean isSuccess) {
        return PasswordCodeResponse.builder()
                .success(isSuccess)
                .message("인증코드가 이메일로 전송되었습니다.")
                .build();
    }
}
