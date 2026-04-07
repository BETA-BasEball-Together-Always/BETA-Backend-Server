package com.beta.controller.post.response;

import com.beta.community.application.admin.dto.AdminPostDetailResult;
import com.beta.community.domain.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 게시글 상세 응답")
public record AdminPostDetailResponse(
        Long postId,
        String content,
        String channel,
        Status status,
        List<ImageResponse> images,
        List<String> hashtags,
        AuthorInfo author,
        EmotionCount emotions,
        Integer commentCount,
        LocalDateTime createdAt,
        List<CommentResponse> comments,
        boolean hasNextComments,
        Long nextCommentCursor
) {
    public record ImageResponse(
            Long imageId,
            String imageUrl
    ) {
        static ImageResponse from(AdminPostDetailResult.ImageResult imageInfo) {
            return new ImageResponse(
                    imageInfo.imageId(),
                    imageInfo.imageUrl()
            );
        }
    }

    public record AuthorInfo(
            Long userId,
            String nickname,
            String teamCode
    ) {
    }

    public record EmotionCount(
            Integer likeCount,
            Integer sadCount,
            Integer funCount,
            Integer hypeCount
    ) {
    }

    public record CommentResponse(
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
            List<ReplyResponse> replies
    ) {
    }

    public record ReplyResponse(
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

    public static AdminPostDetailResponse from(AdminPostDetailResult dto) {
        return new AdminPostDetailResponse(
                dto.postId(),
                dto.content(),
                dto.channel().name(),
                dto.status(),
                dto.images().stream()
                        .map(ImageResponse::from)
                        .toList(),
                dto.hashtags(),
                new AuthorInfo(
                        dto.author().userId(),
                        dto.author().nickname(),
                        dto.author().teamCode()
                ),
                new EmotionCount(
                        dto.emotions().likeCount(),
                        dto.emotions().sadCount(),
                        dto.emotions().funCount(),
                        dto.emotions().hypeCount()
                ),
                dto.commentCount(),
                dto.createdAt(),
                toCommentResponses(dto.comments()),
                dto.hasNextComments(),
                dto.nextCommentCursor()
        );
    }

    static List<CommentResponse> toCommentResponses(List<AdminPostDetailResult.CommentResult> comments) {
        if (comments == null) {
            return List.of();
        }

        return comments.stream()
                .map(AdminPostDetailResponse::toCommentResponse)
                .toList();
    }

    static CommentResponse toCommentResponse(AdminPostDetailResult.CommentResult dto) {
        return new CommentResponse(
                dto.commentId(),
                dto.userId(),
                dto.nickname(),
                dto.teamCode(),
                dto.content(),
                dto.likeCount(),
                dto.depth(),
                dto.createdAt(),
                dto.isLiked(),
                dto.deleted(),
                toReplyResponses(dto.replies())
        );
    }

    static List<ReplyResponse> toReplyResponses(List<AdminPostDetailResult.ReplyResult> replies) {
        if (replies == null) {
            return List.of();
        }

        return replies.stream()
                .map(AdminPostDetailResponse::toReplyResponse)
                .toList();
    }

    static ReplyResponse toReplyResponse(AdminPostDetailResult.ReplyResult dto) {
        return new ReplyResponse(
                dto.commentId(),
                dto.userId(),
                dto.nickname(),
                dto.teamCode(),
                dto.content(),
                dto.likeCount(),
                dto.depth(),
                dto.createdAt(),
                dto.isLiked(),
                dto.deleted()
        );
    }
}
