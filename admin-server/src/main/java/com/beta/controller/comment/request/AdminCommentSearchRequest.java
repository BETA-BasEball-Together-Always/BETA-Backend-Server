package com.beta.controller.comment.request;

import com.beta.community.domain.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 댓글 조회 검색 조건")
public record AdminCommentSearchRequest(
        @Schema(description = "댓글 상태 필터", example = "ACTIVE", nullable = true)
        Status status,
        @Schema(description = "본문 검색어", example = "댓글", nullable = true)
        String keyword
) {
    public String keywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
