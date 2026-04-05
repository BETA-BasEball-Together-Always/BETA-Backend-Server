package com.beta.community.application.admin.dto;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;

import java.time.LocalDateTime;
import java.util.List;

public record AdminPostDetailResult(
        Long postId,
        AuthorResult author,
        String content,
        Post.Channel channel,
        Status status,
        List<ImageResult> images,
        List<String> hashtags,
        EmotionResult emotions,
        Integer commentCount,
        LocalDateTime createdAt,
        List<CommentResult> comments,
        boolean hasNextComments,
        Long nextCommentCursor
) {
    public record AuthorResult(
            Long userId,
            String nickname,
            String teamCode
    ) {
    }

    public record ImageResult(
            Long imageId,
            String imageUrl
    ) {
    }

    public record EmotionResult(
            Integer likeCount,
            Integer sadCount,
            Integer funCount,
            Integer hypeCount
    ) {
    }

    public record CommentResult(
            Long commentId,
            Long userId,
            String nickname,
            String teamCode,
            String content,
            Integer likeCount,
            Integer depth,
            LocalDateTime createdAt,
            boolean isLiked,
            boolean deleted,
            List<ReplyResult> replies
    ) {
    }

    public record ReplyResult(
            Long commentId,
            Long userId,
            String nickname,
            String teamCode,
            String content,
            Integer likeCount,
            Integer depth,
            LocalDateTime createdAt,
            boolean isLiked,
            boolean deleted
    ) {
    }
}
