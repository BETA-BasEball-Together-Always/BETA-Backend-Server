package com.beta.controller.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "관리자 페이지 응답")
public record AdminPageResponse<T>(
        @Schema(description = "조회 결과 목록")
        List<T> items,
        @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int size,
        @Schema(description = "전체 데이터 수", example = "25")
        long totalCount,
        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages
) {
    public static <T> AdminPageResponse<T> from(Page<T> page) {
        return new AdminPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
