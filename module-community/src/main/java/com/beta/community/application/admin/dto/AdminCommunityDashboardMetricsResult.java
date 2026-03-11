package com.beta.community.application.admin.dto;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.Post;
import com.beta.community.infra.repository.DashboardQueryRepository;
import com.beta.core.port.dto.AuthorInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AdminCommunityDashboardMetricsResult(
        Long todayPostCount,
        Long todayPostDelta,
        List<RealtimeFeedItem> realtimeFeeds,
        List<PopularTopicItem> popularTopics
) {
    private static final int CONTENT_PREVIEW_LENGTH = 80;

    public static AdminCommunityDashboardMetricsResult from(
            DashboardQueryRepository.PostMetricsSnapshot postMetrics,
            List<Post> realtimeFeedPosts,
            Map<Long, AuthorInfo> authorMap,
            Map<Long, String> thumbnailUrlMap,
            List<Hashtag> hashtags
    ) {
        long todayPostDelta = postMetrics.todayPostCount() - postMetrics.yesterdaySameTimePostCount();

        List<RealtimeFeedItem> realtimeFeeds = realtimeFeedPosts.stream()
                .map(post -> RealtimeFeedItem.from(
                        post,
                        authorMap.getOrDefault(post.getUserId(), AuthorInfo.unknown(post.getUserId())),
                        thumbnailUrlMap.get(post.getId())
                ))
                .toList();

        List<PopularTopicItem> popularTopics = hashtags.stream()
                .map(PopularTopicItem::from)
                .toList();

        return new AdminCommunityDashboardMetricsResult(
                postMetrics.todayPostCount(),
                todayPostDelta,
                realtimeFeeds,
                popularTopics
        );
    }

    private static String toContentPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String compact = content.replaceAll("\\s+", " ").trim();
        if (compact.length() <= CONTENT_PREVIEW_LENGTH) {
            return compact;
        }
        return compact.substring(0, CONTENT_PREVIEW_LENGTH) + "...";
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
        public static RealtimeFeedItem from(Post post, AuthorInfo author, String thumbnailUrl) {
            return new RealtimeFeedItem(
                    post.getId(),
                    author.getNickname(),
                    toContentPreview(post.getContent()),
                    post.getChannel().name(),
                    post.getCreatedAt(),
                    post.getLikeCount(),
                    post.getCommentCount(),
                    thumbnailUrl
            );
        }
    }

    public record PopularTopicItem(
            Long hashtagId,
            String hashtag,
            Long usageCount
    ) {
        public static PopularTopicItem from(Hashtag hashtag) {
            return new PopularTopicItem(
                    hashtag.getId(),
                    hashtag.getTagName(),
                    hashtag.getUsageCount()
            );
        }
    }
}
