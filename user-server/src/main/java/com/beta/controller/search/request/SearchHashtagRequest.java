package com.beta.controller.search.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "해시태그 검색 요청")
public record SearchHashtagRequest(
        @Schema(description = "검색어 (최대 30자)", example = "야구", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 30, message = "검색어는 30자 이하여야 합니다.")
        String keyword,
        @Schema(description = "커서 점수 (다음 페이지 조회 시)")
        Float cursorScore,
        @Schema(description = "커서 ID (다음 페이지 조회 시)")
        Long cursorId
) {
}
