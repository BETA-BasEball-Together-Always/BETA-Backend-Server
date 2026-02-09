package com.beta.controller.account.response;

import com.beta.account.application.dto.SignupStepResult;
import com.beta.account.application.dto.TeamDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "프로필 설정 응답")
public class SignupProfileResponse {

    @Schema(description = "현재 회원가입 단계", example = "PROFILE_COMPLETED")
    private String signupStep;

    @Schema(description = "선택 가능한 야구팀 목록")
    private List<TeamDto> teamList;

    public static SignupProfileResponse from(SignupStepResult result) {
        return SignupProfileResponse.builder()
                .signupStep(result.getSignupStep().name())
                .teamList(result.getTeamList())
                .build();
    }
}
