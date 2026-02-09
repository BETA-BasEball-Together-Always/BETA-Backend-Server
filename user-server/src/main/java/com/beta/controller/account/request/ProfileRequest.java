package com.beta.controller.account.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileRequest {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 13, message = "닉네임은 2~13자 사이여야 합니다")
    private String nickname;
}
