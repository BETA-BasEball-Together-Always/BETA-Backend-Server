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
    private final String email;

    public static SignupStepResult of(Long userId, SignupStep signupStep) {
        return SignupStepResult.builder()
                .userId(userId)
                .signupStep(signupStep)
                .build();
    }

    public static SignupStepResult withEmail(Long userId, SignupStep signupStep, String email) {
        return SignupStepResult.builder()
                .userId(userId)
                .signupStep(signupStep)
                .email(email)
                .build();
    }

    public static SignupStepResult withTeamList(Long userId, SignupStep signupStep, List<BaseballTeam> teamList) {
        return SignupStepResult.builder()
                .userId(userId)
                .signupStep(signupStep)
                .teamList(TeamDto.fromList(teamList))
                .build();
    }
}
