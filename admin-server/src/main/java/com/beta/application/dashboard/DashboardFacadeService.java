package com.beta.application.dashboard;

import com.beta.account.application.admin.AdminAccountDashboardFacadeService;
import com.beta.account.application.admin.dto.AdminAccountDashboardMetricsResult;
import com.beta.application.dashboard.dto.AdminDashboardResult;
import com.beta.community.application.admin.AdminCommunityDashboardFacadeService;
import com.beta.community.application.admin.dto.AdminCommunityDashboardMetricsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardFacadeService {

    private static final int REALTIME_FEED_LIMIT = 5;
    private static final int POPULAR_TOPIC_LIMIT = 3;

    private final AdminAccountDashboardFacadeService adminAccountDashboardFacadeService;
    private final AdminCommunityDashboardFacadeService adminCommunityDashboardFacadeService;

    @Transactional(readOnly = true)
    public AdminDashboardResult getDashboard() {
        AdminAccountDashboardMetricsResult accountMetrics =
                adminAccountDashboardFacadeService.getDashboardAccountMetrics();
        AdminCommunityDashboardMetricsResult communityMetrics =
                adminCommunityDashboardFacadeService.getDashboardCommunityMetrics(
                REALTIME_FEED_LIMIT,
                POPULAR_TOPIC_LIMIT
        );

        Long pendingReportCount = 0L; // TODO : 신고 로직 없음

        return AdminDashboardResult.from(accountMetrics, communityMetrics, pendingReportCount);
    }
}
