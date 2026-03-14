package com.beta.account.application.admin.dto;

import com.beta.account.infra.repository.ChannelOverviewMemberQueryRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AdminAccountChannelOverviewMetricsResult(
        Map<String, Long> memberCountByTeamCode
) {
    public static AdminAccountChannelOverviewMetricsResult from(
            List<ChannelOverviewMemberQueryRepository.TeamMemberCountSnapshot> snapshots
    ) {
        Map<String, Long> memberCountByTeamCode = snapshots.stream()
                .collect(Collectors.toMap(
                        ChannelOverviewMemberQueryRepository.TeamMemberCountSnapshot::teamCode,
                        ChannelOverviewMemberQueryRepository.TeamMemberCountSnapshot::memberCount,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        return new AdminAccountChannelOverviewMetricsResult(memberCountByTeamCode);
    }

    public long getMemberCount(String teamCode) {
        return memberCountByTeamCode.getOrDefault(teamCode, 0L);
    }
}
