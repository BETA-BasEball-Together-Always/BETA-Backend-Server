package com.beta.controller.post.response;

import com.beta.community.application.admin.dto.AdminPostQueryResult;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 게시글 조회 항목")
public record AdminPostItemResponse(
        @Schema(description = "게시글 ID", example = "101")
        Long postId,
        @Schema(description = "작성자 사용자 ID", example = "2")
        Long authorUserId,
        @Schema(description = "작성자 닉네임", example = "slugger2", nullable = true)
        String authorNickname,
        @Schema(description = "게시글 본문", example = "오늘 경기 직관 후기입니다.")
        String content,
        @Schema(description = "채널", example = "LG")
        Post.Channel channel,
        @Schema(description = "게시글 상태", example = "ACTIVE")
        Status status,
        @Schema(description = "작성일시", example = "2026-03-15T12:30:00")
        LocalDateTime createdAt
) {
    public static AdminPostItemResponse from(AdminPostQueryResult result) {
        return new AdminPostItemResponse(
                result.postId(),
                result.authorUserId(),
                result.authorNickname(),
                result.content(),
                result.channel(),
                result.status(),
                result.createdAt()
        );
    }
}
