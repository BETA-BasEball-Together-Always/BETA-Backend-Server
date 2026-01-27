package com.beta.controller.account.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ConsentRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "개인정보 수집 및 이용 동의는 필수입니다")
    @AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다")
    private Boolean personalInfoRequired;

    private Boolean agreeMarketing;
}
