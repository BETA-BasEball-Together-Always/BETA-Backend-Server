package com.beta.account.application.admin.dto;

public record AdminAccountDashboardMetricsResult(
        Long totalUserCount,
        Long totalUserDelta,
        Long todayNewSignupCount,
        Long todayNewSignupDelta
) {
}
