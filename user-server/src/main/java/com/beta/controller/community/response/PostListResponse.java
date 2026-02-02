package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PostListResponse {

    private List<PostSummary> posts;
    private boolean hasNext;
    private Long nextCursor;

    @Getter
    @Builder
    public static class PostSummary {
        private Long postId;
        private String content;
        private String channel;
        private List<String> imageUrls;
        private List<String> hashtags;
        private AuthorInfo author;
        private EmotionCount emotions;
        private Integer commentCount;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long userId;
        private String nickname;
        private String teamCode;
    }

    @Getter
    @Builder
    public static class EmotionCount {
        private Integer likeCount;
        private Integer sadCount;
        private Integer funCount;
        private Integer hypeCount;
    }
}
