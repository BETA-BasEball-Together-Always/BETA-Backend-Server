package com.beta.controller.channel.response;

import com.beta.application.channel.dto.AdminChannelOverviewResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "관리자 팀별 현황 응답")
public record AdminChannelOverviewResponse(
        @Schema(description = "오늘 최다 활동 팀")
        PeakTeam todayPeakTeam,
        @Schema(description = "최근 7일 최다 활동 팀")
        PeakTeam weeklyPeakTeam,
        @Schema(description = "팀별 현황 목록")
        List<TeamActivity> teams
) {
    public static AdminChannelOverviewResponse from(AdminChannelOverviewResult result) {
        return new AdminChannelOverviewResponse(
                PeakTeam.from(result.todayPeakTeam()),
                PeakTeam.from(result.weeklyPeakTeam()),
                result.teams().stream()
                        .map(TeamActivity::from)
                        .toList()
        );
    }

    @Schema(description = "최다 활동 팀 요약")
    public record PeakTeam(
            @Schema(description = "팀 코드", example = "LG", nullable = true)
            String teamCode,
            @Schema(description = "팀명", example = "LG 트윈스", nullable = true)
            String teamName,
            @Schema(description = "활동 수치", example = "138")
            long activityCount,
            @Schema(description = "선정 기준 기간의 게시물 수", example = "42")
            long postCount,
            @Schema(description = "선정 기준 기간의 댓글 수", example = "96")
            long commentCount
    ) {
        private static PeakTeam from(AdminChannelOverviewResult.PeakTeam peakTeam) {
            return new PeakTeam(
                    peakTeam.teamCode(),
                    peakTeam.teamName(),
                    peakTeam.activityCount(),
                    peakTeam.postCount(),
                    peakTeam.commentCount()
            );
        }
    }

    @Schema(description = "팀별 현황 항목")
    public record TeamActivity(
            @Schema(description = "팀 코드", example = "LG")
            String teamCode,
            @Schema(description = "팀명", example = "LG 트윈스")
            String teamName,
            @Schema(description = "팀 사용자 수", example = "18120")
            long userCount,
            @Schema(description = "오늘 게시물 수", example = "42")
            long todayPostCount,
            @Schema(description = "오늘 댓글 수", example = "96")
            long todayCommentCount,
            @Schema(description = "오늘 총 활동 수(게시물+댓글)", example = "138")
            long todayActivityCount,
            @Schema(description = "최근 7일 게시물 수", example = "227")
            long weeklyPostCount,
            @Schema(description = "최근 7일 댓글 수", example = "301")
            long weeklyCommentCount,
            @Schema(description = "최근 7일 총 활동 수(게시물+댓글)", example = "528")
            long weeklyActivityCount,
            @Schema(description = "최근 7일 일별 활동 목록")
            List<DailyActivity> dailyActivities
    ) {
        private static TeamActivity from(AdminChannelOverviewResult.TeamActivity item) {
            return new TeamActivity(
                    item.teamCode(),
                    item.teamName(),
                    item.userCount(),
                    item.todayPostCount(),
                    item.todayCommentCount(),
                    item.todayActivityCount(),
                    item.weeklyPostCount(),
                    item.weeklyCommentCount(),
                    item.weeklyActivityCount(),
                    item.dailyActivities().stream()
                            .map(DailyActivity::from)
                            .toList()
            );
        }
    }

    @Schema(description = "일별 활동 항목")
    public record DailyActivity(
            @Schema(description = "날짜", example = "2026-03-12")
            LocalDate date,
            @Schema(description = "게시물 수", example = "42")
            long postCount,
            @Schema(description = "댓글 수", example = "96")
            long commentCount,
            @Schema(description = "총 활동 수", example = "138")
            long totalActivityCount
    ) {
        private static DailyActivity from(AdminChannelOverviewResult.DailyActivity item) {
            return new DailyActivity(
                    item.date(),
                    item.postCount(),
                    item.commentCount(),
                    item.totalActivityCount()
            );
        }
    }
}
