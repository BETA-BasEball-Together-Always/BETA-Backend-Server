package com.beta.controller.account.response;

import com.beta.account.application.dto.SignupStepResult;
import com.beta.account.application.dto.TeamDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SignupStepResponse {

    private Long userId;
    private String signupStep;
    private List<TeamDto> teamList;

    public static SignupStepResponse from(SignupStepResult result) {
        return SignupStepResponse.builder()
                .userId(result.getUserId())
                .signupStep(result.getSignupStep().name())
                .teamList(result.getTeamList())
                .build();
    }
}
