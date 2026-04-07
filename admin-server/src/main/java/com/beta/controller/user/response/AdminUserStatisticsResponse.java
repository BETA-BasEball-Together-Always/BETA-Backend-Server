package com.beta.controller.user.response;

import com.beta.account.application.admin.dto.AdminUserStatisticsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 사용자 통계 응답")
public record AdminUserStatisticsResponse(
        @Schema(description = "통계 대상 전체 사용자 수", example = "1280")
        long totalUserCount,
        @Schema(description = "성별 통계")
        List<GenderStat> genderStats,
        @Schema(description = "나이 통계")
        List<AgeStat> ageStats
) {
    public static AdminUserStatisticsResponse from(AdminUserStatisticsResult result) {
        return new AdminUserStatisticsResponse(
                result.totalUserCount(),
                result.genderStats().stream()
                        .map(item -> new GenderStat(item.gender(), item.count()))
                        .toList(),
                result.ageStats().stream()
                        .map(item -> new AgeStat(item.ageGroup(), item.count()))
                        .toList()
        );
    }

    public record GenderStat(
            @Schema(description = "성별 구분값", example = "FEMALE")
            AdminUserStatisticsResult.GenderCategory gender,
            @Schema(description = "사용자 수", example = "540")
            long count
    ) {
    }

    public record AgeStat(
            @Schema(description = "나이대 구분값", example = "TWENTIES")
            AdminUserStatisticsResult.AgeGroup ageGroup,
            @Schema(description = "사용자 수", example = "401")
            long count
    ) {
    }
}
