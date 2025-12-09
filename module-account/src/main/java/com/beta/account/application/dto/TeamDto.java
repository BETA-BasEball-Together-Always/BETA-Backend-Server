package com.beta.account.application.dto;

import com.beta.account.domain.entity.BaseballTeam;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamDto {
    private String teamCode;
    private String teamNameKr;
    private String teamNameEn;

    public static List<TeamDto> fromList(List<BaseballTeam> teamList) {
        return teamList.stream()
                .map(team -> TeamDto.builder()
                        .teamCode(team.getCode())
                        .teamNameKr(team.getTeamNameKr())
                        .teamNameEn(team.getTeamNameEn())
                        .build())
                .toList();
    }
}
