package com.beta.account.application.dto;

import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.SignupStep;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SignupStepResult {
    private final Long userId;
    private final SignupStep signupStep;
    private final List<TeamDto> teamList;
    private final String accessToken;

    public static SignupStepResult of(Long userId, SignupStep signupStep) {
        return SignupStepResult.builder()
                .userId(userId)
                .signupStep(signupStep)
                .build();
    }

    public static SignupStepResult withTeamList(Long userId, SignupStep signupStep, List<BaseballTeam> teamList) {
        return SignupStepResult.builder()
                .userId(userId)
                .signupStep(signupStep)
                .teamList(TeamDto.fromList(teamList))
                .build();
    }

    public static SignupStepResult withAccessToken(Long userId, SignupStep signupStep, String accessToken) {
        return SignupStepResult.builder()
                .userId(userId)
                .signupStep(signupStep)
                .accessToken(accessToken)
                .build();
    }
}
