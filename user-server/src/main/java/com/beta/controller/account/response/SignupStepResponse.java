package com.beta.controller.account.response;

import com.beta.account.application.dto.SignupStepResult;
import com.beta.account.application.dto.TeamDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SignupStepResponse {

    private String signupStep;
    private List<TeamDto> teamList;
    private String accessToken;
    private String email;

    public static SignupStepResponse from(SignupStepResult result) {
        return SignupStepResponse.builder()
                .signupStep(result.getSignupStep().name())
                .teamList(result.getTeamList())
                .accessToken(result.getAccessToken())
                .email(result.getEmail())
                .build();
    }
}
