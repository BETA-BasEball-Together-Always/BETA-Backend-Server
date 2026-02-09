package com.beta.controller.account.response;

import com.beta.account.application.dto.SignupStepResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "팀 선택 응답")
public class SignupTeamResponse {

    @Schema(description = "현재 회원가입 단계", example = "TEAM_SELECTED")
    private String signupStep;

    public static SignupTeamResponse from(SignupStepResult result) {
        return SignupTeamResponse.builder()
                .signupStep(result.getSignupStep().name())
                .build();
    }
}
