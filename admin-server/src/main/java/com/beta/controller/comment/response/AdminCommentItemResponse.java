package com.beta.controller.comment.response;

import com.beta.community.application.admin.dto.AdminCommentQueryResult;
import com.beta.community.domain.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 댓글 조회 항목")
public record AdminCommentItemResponse(
        @Schema(description = "댓글 ID", example = "201")
        Long commentId,
        @Schema(description = "작성자 사용자 ID", example = "4")
        Long authorUserId,
        @Schema(description = "작성자 닉네임", example = "comment-user", nullable = true)
        String authorNickname,
        @Schema(description = "게시글 ID", example = "101")
        Long postId,
        @Schema(description = "댓글 본문", example = "좋은 경기였습니다.")
        String content,
        @Schema(description = "댓글 depth", example = "0")
        Integer depth,
        @Schema(description = "댓글 상태", example = "ACTIVE")
        Status status,
        @Schema(description = "작성일시", example = "2026-03-15T12:30:00")
        LocalDateTime createdAt
) {
    public static AdminCommentItemResponse from(AdminCommentQueryResult result) {
        return new AdminCommentItemResponse(
                result.commentId(),
                result.authorUserId(),
                result.authorNickname(),
                result.postId(),
                result.content(),
                result.depth(),
                result.status(),
                result.createdAt()
        );
    }
}
