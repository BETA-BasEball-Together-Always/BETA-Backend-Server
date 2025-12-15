package com.beta.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordResponse {
    private boolean success;
    private String message;

    public static ResetPasswordResponse success(boolean isSuccess) {
        return ResetPasswordResponse.builder()
                .success(isSuccess)
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .build();
    }
}
