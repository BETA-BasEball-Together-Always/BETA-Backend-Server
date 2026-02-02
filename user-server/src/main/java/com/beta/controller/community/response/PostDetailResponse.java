package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {

    private Long postId;
    private String content;
    private String channel;
    private List<String> imageUrls;
    private List<String> hashtags;
    private PostListResponse.AuthorInfo author;
    private PostListResponse.EmotionCount emotions;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private List<CommentResponse> comments;

    @Getter
    @Builder
    public static class CommentResponse {
        private Long commentId;
        private Long userId;
        private String nickname;
        private String content;
        private Integer likeCount;
        private Integer depth;
        private LocalDateTime createdAt;
        private List<CommentResponse> replies;
    }
}
