package com.beta.controller.account.response;

import com.beta.account.application.dto.SignupStepResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "약관 동의 응답")
public class SignupConsentResponse {

    @Schema(description = "현재 회원가입 단계", example = "CONSENT_AGREED")
    private String signupStep;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    public static SignupConsentResponse from(SignupStepResult result) {
        return SignupConsentResponse.builder()
                .signupStep(result.getSignupStep().name())
                .email(result.getEmail())
                .build();
    }
}
