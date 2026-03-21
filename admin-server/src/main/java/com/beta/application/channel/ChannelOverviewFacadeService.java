package com.beta.application.channel;

import com.beta.account.application.admin.AdminAccountChannelOverviewAppService;
import com.beta.account.application.admin.dto.AdminAccountChannelOverviewMetricsResult;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.service.BaseballTeamReadService;
import com.beta.application.channel.dto.AdminChannelOverviewResult;
import com.beta.community.application.admin.AdminCommunityChannelOverviewFacadeService;
import com.beta.community.application.admin.dto.AdminCommunityChannelOverviewMetricsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelOverviewFacadeService {

    private static final int TREND_DAYS = 7;

    private final BaseballTeamReadService baseballTeamReadService;
    private final AdminAccountChannelOverviewAppService adminAccountChannelOverviewAppService;
    private final AdminCommunityChannelOverviewFacadeService adminCommunityChannelOverviewFacadeService;

    @Transactional(readOnly = true)
    public AdminChannelOverviewResult getChannelOverview() {
        List<BaseballTeam> baseballTeams = baseballTeamReadService.getAllBaseballTeams();
        List<String> teamCodes = baseballTeams.stream()
                .map(BaseballTeam::getCode)
                .toList();

        AdminAccountChannelOverviewMetricsResult accountMetrics =
                adminAccountChannelOverviewAppService.getChannelOverviewAccountMetrics();
        AdminCommunityChannelOverviewMetricsResult communityMetrics =
                adminCommunityChannelOverviewFacadeService.getChannelOverviewMetrics(teamCodes, TREND_DAYS);

        return AdminChannelOverviewResult.from(baseballTeams, accountMetrics, communityMetrics);
    }
}
