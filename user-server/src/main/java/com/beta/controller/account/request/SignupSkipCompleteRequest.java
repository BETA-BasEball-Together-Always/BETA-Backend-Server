package com.beta.controller.account.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignupSkipCompleteRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
}
