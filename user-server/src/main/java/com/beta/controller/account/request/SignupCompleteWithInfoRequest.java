package com.beta.controller.account.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SignupCompleteWithInfoRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F만 허용됩니다")
    private String gender;

    private Integer age;
}
