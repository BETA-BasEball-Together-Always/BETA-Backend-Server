package com.beta.controller.post.response;

import com.beta.community.application.admin.dto.AdminPostCommentsResult;
import com.beta.community.application.admin.dto.AdminPostDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 게시글 댓글 목록 응답")
public record AdminPostCommentsResponse(
        List<AdminPostDetailResponse.CommentResponse> comments,
        boolean hasNext,
        Long nextCursor
) {
    public static AdminPostCommentsResponse from(AdminPostCommentsResult dto) {
        return new AdminPostCommentsResponse(
                toCommentResponses(dto.comments()),
                dto.hasNext(),
                dto.nextCursor()
        );
    }

    static List<AdminPostDetailResponse.CommentResponse> toCommentResponses(List<AdminPostDetailResult.CommentResult> comments) {
        if (comments == null) {
            return List.of();
        }

        return comments.stream()
                .map(AdminPostCommentsResponse::toCommentResponse)
                .toList();
    }

    static AdminPostDetailResponse.CommentResponse toCommentResponse(AdminPostDetailResult.CommentResult dto) {
        return new AdminPostDetailResponse.CommentResponse(
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
                AdminPostDetailResponse.toReplyResponses(dto.replies())
        );
    }
}
