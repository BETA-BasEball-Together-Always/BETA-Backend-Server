package com.beta.community.application.dto;

import com.beta.core.port.dto.AuthorInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListDto {

    private List<PostSummaryDto> posts;
    private boolean hasNext;
    private Long nextCursor;

    @Getter
    @Builder
    public static class PostSummaryDto {
        private Long postId;
        private AuthorInfo author;
        private String content;
        private String channel;
        private List<String> imageUrls;
        private List<String> hashtags;
        private Integer likeCount;
        private Integer sadCount;
        private Integer funCount;
        private Integer hypeCount;
        private Integer commentCount;
        private LocalDateTime createdAt;
    }
}
