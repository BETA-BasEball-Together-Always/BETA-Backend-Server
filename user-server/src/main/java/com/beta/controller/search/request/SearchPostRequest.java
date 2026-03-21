package com.beta.controller.search.request;

import com.beta.search.domain.sort.SearchPostSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 검색 요청")
public record SearchPostRequest(
        @Schema(description = "검색어 (최대 30자)", example = "야구", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 30, message = "검색어는 30자 이하여야 합니다.")
        String keyword,
        @Schema(description = "채널 구분 (ALL: 전체, TEAM: 내 팀)", example = "ALL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "채널은 필수입니다.")
        SearchChannel channel,
        @Schema(description = "정렬 방식 (RECOMMENDED: 추천순, LATEST: 최신순, POPULAR: 인기순)", example = "RECOMMENDED")
        SearchPostSort sort,
        @Schema(description = "커서 점수 (다음 페이지 조회 시)")
        Double cursorScore,
        @Schema(description = "커서 ID (다음 페이지 조회 시)")
        Long cursorId
) {
}
