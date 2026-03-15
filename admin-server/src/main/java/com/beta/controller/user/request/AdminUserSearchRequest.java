package com.beta.controller.user.request;

import com.beta.account.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 사용자 조회 검색 조건")
public record AdminUserSearchRequest(
        @Schema(description = "사용자 상태 필터", example = "ACTIVE", nullable = true)
        User.UserStatus status,
        @Schema(description = "닉네임 또는 이메일 검색어", example = "slugger", nullable = true)
        String keyword
) {
    public String keywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
