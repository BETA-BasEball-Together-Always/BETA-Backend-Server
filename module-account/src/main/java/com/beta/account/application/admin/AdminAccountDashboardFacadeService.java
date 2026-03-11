package com.beta.account.application.admin;

import com.beta.account.application.admin.dto.AdminAccountDashboardMetricsResult;
import com.beta.account.infra.repository.DashboardMetricsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountDashboardFacadeService {

    private final DashboardMetricsQueryRepository dashboardMetricsQueryRepository;

    @Transactional(readOnly = true)
    public AdminAccountDashboardMetricsResult getDashboardAccountMetrics() {
        DashboardMetricsQueryRepository.DashboardMetricsSnapshot snapshot =
                dashboardMetricsQueryRepository.getDashboardMetricsSnapshot();

        long totalMemberDelta = snapshot.todayActiveSignups() - snapshot.todayWithdrawnUsers();
        long todayNewSignupDelta = snapshot.todayNewSignupCount() - snapshot.yesterdayNewSignupCount();

        return new AdminAccountDashboardMetricsResult(
                snapshot.totalMemberCount(),
                totalMemberDelta,
                snapshot.todayNewSignupCount(),
                todayNewSignupDelta
        );
    }
}
