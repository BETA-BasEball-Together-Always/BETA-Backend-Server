package com.beta.account.domain.service;

import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.infra.repository.BaseballTeamRepository;
import com.beta.core.exception.ErrorCode;
import com.beta.core.exception.account.TeamNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseballTeamReadService 단위 테스트")
class BaseballTeamReadServiceTest {

    @Mock
    private BaseballTeamRepository baseballTeamRepository;

    @InjectMocks
    private BaseballTeamReadService baseballTeamReadService;

    @Test
    @DisplayName("모든 야구팀 목록을 조회한다")
    void getAllBaseballTeams_ReturnsAllTeams() {
        // given
        List<BaseballTeam> teamList = Arrays.asList(
                createBaseballTeam("SSG", "SSG 랜더스"),
                createBaseballTeam("KIA", "KIA 타이거즈"),
                createBaseballTeam("LG", "LG 트윈스"),
                createBaseballTeam("KT", "KT 위즈"),
                createBaseballTeam("NC", "NC 다이노스")
        );

        when(baseballTeamRepository.findAll()).thenReturn(teamList);

        // when
        List<BaseballTeam> result = baseballTeamReadService.getAllBaseballTeams();

        // then
        assertThat(result).hasSize(5);
        assertThat(result).extracting("code")
                .containsExactly("SSG", "KIA", "LG", "KT", "NC");
        assertThat(result).extracting("teamNameKr")
                .containsExactly("SSG 랜더스", "KIA 타이거즈", "LG 트윈스", "KT 위즈", "NC 다이노스");

        verify(baseballTeamRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("팀 코드로 야구팀을 조회한다")
    void getBaseballTeamById_ReturnsTeam_WhenTeamExists() {
        // given
        String teamCode = "SSG";
        BaseballTeam expectedTeam = createBaseballTeam("SSG", "SSG 랜더스");

        when(baseballTeamRepository.findById(teamCode)).thenReturn(Optional.of(expectedTeam));

        // when
        BaseballTeam result = baseballTeamReadService.getBaseballTeamById(teamCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SSG");
        assertThat(result.getTeamNameKr()).isEqualTo("SSG 랜더스");

        verify(baseballTeamRepository, times(1)).findById(teamCode);
    }

    @Test
    @DisplayName("존재하지 않는 팀 코드로 조회 시 TeamNotFoundException을 발생시킨다")
    void getBaseballTeamById_ThrowsTeamNotFoundException_WhenTeamNotFound() {
        // given
        String invalidTeamCode = "INVALID";

        when(baseballTeamRepository.findById(invalidTeamCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> baseballTeamReadService.getBaseballTeamById(invalidTeamCode))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("해당 구단은 존재하지 않습니다 : " + invalidTeamCode)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEAM_NOT_FOUND);

        verify(baseballTeamRepository, times(1)).findById(invalidTeamCode);
    }

    private BaseballTeam createBaseballTeam(String code, String name) {
        return BaseballTeam.builder()
                .code(code)
                .teamNameKr(name)
                .build();
    }
}
