package com.beta.community.application.admin.dto;

import com.beta.community.infra.repository.ChannelOverviewQueryRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record AdminCommunityChannelOverviewMetricsResult(
        List<ChannelActivityMetrics> channels
) {
    public static AdminCommunityChannelOverviewMetricsResult from(
            List<String> channelCodes,
            List<LocalDate> dates,
            List<ChannelOverviewQueryRepository.ChannelDailyCountSnapshot> postSnapshots,
            List<ChannelOverviewQueryRepository.ChannelDailyCountSnapshot> commentSnapshots
    ) {
        Map<String, Map<LocalDate, Long>> postCountMap = buildDailyCountMap(postSnapshots);
        Map<String, Map<LocalDate, Long>> commentCountMap = buildDailyCountMap(commentSnapshots);

        List<ChannelActivityMetrics> channels = channelCodes.stream()
                .map(channelCode -> toChannelActivityMetrics(
                        channelCode,
                        dates,
                        postCountMap.getOrDefault(channelCode, Map.of()),
                        commentCountMap.getOrDefault(channelCode, Map.of())
                ))
                .toList();

        return new AdminCommunityChannelOverviewMetricsResult(channels);
    }

    private static Map<String, Map<LocalDate, Long>> buildDailyCountMap(
            List<ChannelOverviewQueryRepository.ChannelDailyCountSnapshot> snapshots
    ) {
        Map<String, Map<LocalDate, Long>> result = new LinkedHashMap<>();

        for (ChannelOverviewQueryRepository.ChannelDailyCountSnapshot snapshot : snapshots) {
            result.computeIfAbsent(snapshot.channelCode(), key -> new LinkedHashMap<>())
                    .put(snapshot.date(), snapshot.count());
        }

        return result;
    }

    private static ChannelActivityMetrics toChannelActivityMetrics(
            String channelCode,
            List<LocalDate> dates,
            Map<LocalDate, Long> postCountByDate,
            Map<LocalDate, Long> commentCountByDate
    ) {
        List<DailyActivity> dailyActivities = dates.stream()
                .map(date -> new DailyActivity(
                        date,
                        postCountByDate.getOrDefault(date, 0L),
                        commentCountByDate.getOrDefault(date, 0L)
                ))
                .toList();

        DailyActivity todayActivity = dailyActivities.isEmpty()
                ? new DailyActivity(LocalDate.now(), 0L, 0L)
                : dailyActivities.get(dailyActivities.size() - 1);

        long weeklyPostCount = dailyActivities.stream()
                .mapToLong(DailyActivity::postCount)
                .sum();
        long weeklyCommentCount = dailyActivities.stream()
                .mapToLong(DailyActivity::commentCount)
                .sum();

        return new ChannelActivityMetrics(
                channelCode,
                todayActivity.postCount(),
                todayActivity.commentCount(),
                weeklyPostCount,
                weeklyCommentCount,
                dailyActivities
        );
    }

    public record ChannelActivityMetrics(
            String channelCode,
            long todayPostCount,
            long todayCommentCount,
            long weeklyPostCount,
            long weeklyCommentCount,
            List<DailyActivity> dailyActivities
    ) {
        public long todayActivityCount() {
            return todayPostCount + todayCommentCount;
        }

        public long weeklyActivityCount() {
            return weeklyPostCount + weeklyCommentCount;
        }
    }

    public record DailyActivity(
            LocalDate date,
            long postCount,
            long commentCount
    ) {
        public long totalActivityCount() {
            return postCount + commentCount;
        }
    }
}
