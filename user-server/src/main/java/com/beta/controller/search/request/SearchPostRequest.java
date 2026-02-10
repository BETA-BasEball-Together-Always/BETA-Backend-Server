package com.beta.controller.search.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SearchPostRequest(
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 30, message = "검색어는 30자 이하여야 합니다.")
        String keyword,
        @NotNull(message = "채널은 필수입니다.")
        SearchChannel channel,
        Float cursorScore,
        Long cursorId
) {
}
