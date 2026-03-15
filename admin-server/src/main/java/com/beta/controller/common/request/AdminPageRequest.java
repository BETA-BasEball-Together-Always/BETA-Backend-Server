package com.beta.controller.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "관리자 페이지 요청")
public record AdminPageRequest(
        @Schema(description = "페이지 번호(0부터 시작)", example = "0", defaultValue = "0")
        @Min(value = 0, message = "page는 0 이상이어야 합니다.")
        Integer page,
        @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
        @Min(value = 1, message = "size는 1 이상이어야 합니다.")
        @Max(value = 100, message = "size는 100 이하여야 합니다.")
        Integer size
) {
    public int pageOrDefault() {
        return page == null ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null ? 10 : size;
    }
}
