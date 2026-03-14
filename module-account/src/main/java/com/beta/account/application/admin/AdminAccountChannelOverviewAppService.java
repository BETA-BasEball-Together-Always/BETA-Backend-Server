package com.beta.account.application.admin;

import com.beta.account.application.admin.dto.AdminAccountChannelOverviewMetricsResult;
import com.beta.account.infra.repository.ChannelOverviewMemberQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountChannelOverviewAppService {

    private final ChannelOverviewMemberQueryRepository channelOverviewMemberQueryRepository;

    @Transactional(readOnly = true)
    public AdminAccountChannelOverviewMetricsResult getChannelOverviewAccountMetrics() {
        return AdminAccountChannelOverviewMetricsResult.from(
                channelOverviewMemberQueryRepository.findActiveMemberCountsByFavoriteTeamCode()
        );
    }
}
