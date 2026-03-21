package com.beta.application.dashboard.dto;

import com.beta.account.application.admin.dto.AdminAccountDashboardMetricsResult;
import com.beta.community.application.admin.dto.AdminCommunityDashboardMetricsResult;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResult(
        Long totalUserCount,
        Long totalUserDelta,
        Long todayPostCount,
        Long todayPostDelta,
        Long todayNewSignupCount,
        Long todayNewSignupDelta,
        Long pendingReportCount,
        List<RealtimeFeedItem> realtimeFeeds,
        List<PopularTopicItem> popularTopics
) {
    public static AdminDashboardResult from(
            AdminAccountDashboardMetricsResult accountMetrics,
            AdminCommunityDashboardMetricsResult communityMetrics,
            Long pendingReportCount
    ) {
        return new AdminDashboardResult(
                accountMetrics.totalUserCount(),
                accountMetrics.totalUserDelta(),
                communityMetrics.todayPostCount(),
                communityMetrics.todayPostDelta(),
                accountMetrics.todayNewSignupCount(),
                accountMetrics.todayNewSignupDelta(),
                pendingReportCount,
                communityMetrics.realtimeFeeds().stream()
                        .map(RealtimeFeedItem::from)
                        .toList(),
                communityMetrics.popularTopics().stream()
                        .map(PopularTopicItem::from)
                        .toList()
        );
    }

    public record RealtimeFeedItem(
            Long postId,
            String authorNickname,
            String contentPreview,
            String channel,
            LocalDateTime createdAt,
            Integer likeCount,
            Integer commentCount,
            String thumbnailUrl
    ) {
        public static RealtimeFeedItem from(AdminCommunityDashboardMetricsResult.RealtimeFeedItem item) {
            return new RealtimeFeedItem(
                    item.postId(),
                    item.authorNickname(),
                    item.contentPreview(),
                    item.channel(),
                    item.createdAt(),
                    item.likeCount(),
                    item.commentCount(),
                    item.thumbnailUrl()
            );
        }
    }

    public record PopularTopicItem(
            Long hashtagId,
            String hashtag,
            Long usageCount
    ) {
        public static PopularTopicItem from(AdminCommunityDashboardMetricsResult.PopularTopicItem item) {
            return new PopularTopicItem(
                    item.hashtagId(),
                    item.hashtag(),
                    item.usageCount()
            );
        }
    }
}
