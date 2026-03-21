package com.beta.controller.search.response;

import com.beta.search.domain.cursor.SearchCursor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "다음 페이지 조회용 커서")
public record SearchCursorResponse(
        @Schema(description = "다음 페이지 조회용 정렬 기준 값", example = "1.2345")
        Double score,
        @Schema(description = "동점 정렬용 ID", example = "123")
        Long id
) {
    public static SearchCursorResponse from(SearchCursor cursor) {
        if (cursor == null || cursor.isFirst()) {
            return null;
        }

        return new SearchCursorResponse(cursor.getScore(), cursor.getId());
    }
}
