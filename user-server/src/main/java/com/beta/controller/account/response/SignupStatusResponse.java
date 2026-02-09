package com.beta.controller.account.response;

import com.beta.account.application.dto.SignupStepResult;
import com.beta.account.application.dto.TeamDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "회원가입 상태 조회 응답")
public class SignupStatusResponse {

    @Schema(description = "현재 회원가입 단계", example = "PROFILE_COMPLETED")
    private String signupStep;

    @Schema(description = "사용자 이메일 (CONSENT_AGREED 이후 제공)", example = "user@example.com")
    private String email;

    @Schema(description = "선택 가능한 팀 목록 (PROFILE_COMPLETED일 때 제공)")
    private List<TeamDto> teamList;

    public static SignupStatusResponse from(SignupStepResult result) {
        return SignupStatusResponse.builder()
                .signupStep(result.getSignupStep().name())
                .email(result.getEmail())
                .teamList(result.getTeamList())
                .build();
    }
}
