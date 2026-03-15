package com.beta.application.channel.dto;

import com.beta.account.application.admin.dto.AdminAccountChannelOverviewMetricsResult;
import com.beta.account.domain.entity.BaseballTeam;
import com.beta.community.application.admin.dto.AdminCommunityChannelOverviewMetricsResult;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public record AdminChannelOverviewResult(
        PeakTeam todayPeakTeam,
        PeakTeam weeklyPeakTeam,
        List<TeamActivity> teams
) {
    public static AdminChannelOverviewResult from(
            List<BaseballTeam> baseballTeams,
            AdminAccountChannelOverviewMetricsResult accountMetrics,
            AdminCommunityChannelOverviewMetricsResult communityMetrics
    ) {
        Map<String, BaseballTeam> teamMap = baseballTeams.stream()
                .collect(Collectors.toMap(
                        BaseballTeam::getCode,
                        team -> team
                ));

        List<TeamActivity> teams = communityMetrics.channels().stream()
                .map(channelMetrics -> TeamActivity.from(
                        teamMap.get(channelMetrics.channelCode()),
                        accountMetrics.getUserCount(channelMetrics.channelCode()),
                        channelMetrics
                ))
                .sorted(Comparator.comparing(TeamActivity::teamName))
                .toList();

        return new AdminChannelOverviewResult(
                toPeakTeam(
                        teams,
                        TeamActivity::todayActivityCount,
                        TeamActivity::todayPostCount,
                        TeamActivity::todayCommentCount
                ),
                toPeakTeam(
                        teams,
                        TeamActivity::weeklyActivityCount,
                        TeamActivity::weeklyPostCount,
                        TeamActivity::weeklyCommentCount
                ),
                teams
        );
    }

    private static PeakTeam toPeakTeam(
            List<TeamActivity> teams,
            ToLongFunction<TeamActivity> activityExtractor,
            ToLongFunction<TeamActivity> postExtractor,
            ToLongFunction<TeamActivity> commentExtractor
    ) {
        return teams.stream()
                // 최고 활동 팀, 동률이면 게시물 수 -> 팀명 오름차순
                .max(Comparator
                        .comparingLong(activityExtractor)
                        .thenComparingLong(postExtractor)
                        .thenComparing(TeamActivity::teamName, Comparator.reverseOrder()))
                .map(team -> new PeakTeam(
                        team.teamCode(),
                        team.teamName(),
                        activityExtractor.applyAsLong(team),
                        postExtractor.applyAsLong(team),
                        commentExtractor.applyAsLong(team)
                ))
                .orElseGet(PeakTeam::empty);
    }

    public record PeakTeam(
            String teamCode,
            String teamName,
            long activityCount,
            long postCount,
            long commentCount
    ) {
        private static PeakTeam empty() {
            return new PeakTeam(null, null, 0L, 0L, 0L);
        }
    }

    public record TeamActivity(
            String teamCode,
            String teamName,
            long userCount,
            long todayPostCount,
            long todayCommentCount,
            long todayActivityCount,
            long weeklyPostCount,
            long weeklyCommentCount,
            long weeklyActivityCount,
            List<DailyActivity> dailyActivities
    ) {
        private static TeamActivity from(
                BaseballTeam baseballTeam,
                long userCount,
                AdminCommunityChannelOverviewMetricsResult.ChannelActivityMetrics metrics
        ) {
            String teamName = baseballTeam != null && baseballTeam.getTeamNameKr() != null
                    ? baseballTeam.getTeamNameKr()
                    : metrics.channelCode();

            return new TeamActivity(
                    metrics.channelCode(),
                    teamName,
                    userCount,
                    metrics.todayPostCount(),
                    metrics.todayCommentCount(),
                    metrics.todayActivityCount(),
                    metrics.weeklyPostCount(),
                    metrics.weeklyCommentCount(),
                    metrics.weeklyActivityCount(),
                    metrics.dailyActivities().stream()
                            .map(DailyActivity::from)
                            .toList()
            );
        }
    }

    public record DailyActivity(
            LocalDate date,
            long postCount,
            long commentCount,
            long totalActivityCount
    ) {
        private static DailyActivity from(AdminCommunityChannelOverviewMetricsResult.DailyActivity activity) {
            return new DailyActivity(
                    activity.date(),
                    activity.postCount(),
                    activity.commentCount(),
                    activity.totalActivityCount()
            );
        }
    }
}
