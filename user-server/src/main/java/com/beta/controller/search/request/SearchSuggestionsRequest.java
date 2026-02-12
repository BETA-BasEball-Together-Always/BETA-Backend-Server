package com.beta.controller.search.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "검색어 추천 요청")
public record SearchSuggestionsRequest(
        @Schema(description = "검색어 (최대 30자)", example = "야구", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "검색어는 필수입니다")
        @Size(max = 30, message = "검색어는 30자 이하여야 합니다")
        String keyword
) {
}
