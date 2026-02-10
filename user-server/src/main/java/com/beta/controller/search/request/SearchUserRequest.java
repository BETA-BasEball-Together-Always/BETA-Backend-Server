package com.beta.controller.search.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchUserRequest(
        @NotBlank(message = "검색어는 필수입니다")
        @Size(max = 30, message = "검색어는 30자 이하여야 합니다")
        String keyword,
        Float cursorScore,
        Long cursorId
) {
}
