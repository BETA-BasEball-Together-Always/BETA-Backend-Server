package com.beta.controller.community.response;

import com.beta.community.application.dto.PostListDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListResponse {

    private List<PostSummary> posts;
    private boolean hasNext;
    private Long nextCursor;

    public static PostListResponse from(PostListDto dto) {
        List<PostSummary> posts = dto.getPosts().stream()
                .map(PostSummary::from)
                .toList();

        return PostListResponse.builder()
                .posts(posts)
                .hasNext(dto.isHasNext())
                .nextCursor(dto.getNextCursor())
                .build();
    }

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

        public static PostSummary from(PostListDto.PostSummaryDto dto) {
            return PostSummary.builder()
                    .postId(dto.getPostId())
                    .content(dto.getContent())
                    .channel(dto.getChannel())
                    .imageUrls(dto.getImageUrls())
                    .hashtags(dto.getHashtags())
                    .author(AuthorInfo.from(dto.getAuthor()))
                    .emotions(EmotionCount.builder()
                            .likeCount(dto.getLikeCount())
                            .sadCount(dto.getSadCount())
                            .funCount(dto.getFunCount())
                            .hypeCount(dto.getHypeCount())
                            .build())
                    .commentCount(dto.getCommentCount())
                    .createdAt(dto.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long userId;
        private String nickname;
        private String teamCode;

        public static AuthorInfo from(com.beta.core.port.dto.AuthorInfo author) {
            return AuthorInfo.builder()
                    .userId(author.getUserId())
                    .nickname(author.getNickname())
                    .teamCode(author.getTeamCode())
                    .build();
        }
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
