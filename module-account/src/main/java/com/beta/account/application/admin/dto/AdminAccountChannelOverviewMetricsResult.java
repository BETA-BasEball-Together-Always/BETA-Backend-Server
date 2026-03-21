package com.beta.account.application.admin.dto;

import com.beta.account.infra.repository.ChannelOverviewUserQueryRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AdminAccountChannelOverviewMetricsResult(
        Map<String, Long> userCountByTeamCode
) {
    public static AdminAccountChannelOverviewMetricsResult from(
            List<ChannelOverviewUserQueryRepository.TeamUserCountSnapshot> snapshots
    ) {
        Map<String, Long> userCountByTeamCode = snapshots.stream()
                .collect(Collectors.toMap(
                        ChannelOverviewUserQueryRepository.TeamUserCountSnapshot::teamCode,
                        ChannelOverviewUserQueryRepository.TeamUserCountSnapshot::userCount,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        return new AdminAccountChannelOverviewMetricsResult(userCountByTeamCode);
    }

    public long getUserCount(String teamCode) {
        return userCountByTeamCode.getOrDefault(teamCode, 0L);
    }
}
