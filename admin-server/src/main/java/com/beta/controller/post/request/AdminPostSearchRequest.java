package com.beta.controller.post.request;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 게시글 조회 검색 조건")
public record AdminPostSearchRequest(
        @Schema(description = "게시글 상태 필터", example = "ACTIVE", nullable = true)
        Status status,
        @Schema(description = "채널 필터", example = "LG", nullable = true)
        Post.Channel channel,
        @Schema(description = "본문 검색어", example = "경기", nullable = true)
        String keyword
) {
    public String keywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
