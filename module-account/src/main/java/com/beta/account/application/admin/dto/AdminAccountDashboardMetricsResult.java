package com.beta.account.application.admin.dto;

public record AdminAccountDashboardMetricsResult(
        Long totalMemberCount,
        Long totalMemberDelta,
        Long todayNewSignupCount,
        Long todayNewSignupDelta
) {
}
