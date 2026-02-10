package com.beta.community.application.dto;

import com.beta.core.port.dto.AuthorInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailDto {

    private Long postId;
    private AuthorInfo author;
    private String content;
    private String channel;
    private List<ImageInfo> images;
    private List<String> hashtags;
    private Integer likeCount;
    private Integer sadCount;
    private Integer funCount;
    private Integer hypeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private List<CommentDto> comments;
    private boolean hasNextComments;
    private Long nextCommentCursor;

    @Getter
    @Builder
    public static class CommentDto {
        private Long commentId;
        private Long userId;
        private String nickname;
        private String teamCode;
        private String content;
        private Integer likeCount;
        private Integer depth;
        private LocalDateTime createdAt;
        private boolean isLiked;
        private boolean deleted;
        private List<CommentDto> replies;
    }
}
