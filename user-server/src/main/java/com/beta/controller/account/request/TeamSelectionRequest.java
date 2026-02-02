package com.beta.controller.account.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TeamSelectionRequest {

    @NotBlank(message = "응원팀 코드는 필수입니다")
    private String teamCode;
}
