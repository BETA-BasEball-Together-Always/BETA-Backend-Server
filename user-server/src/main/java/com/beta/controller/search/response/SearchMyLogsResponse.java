package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchMyLogResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 검색 기록 응답")
public record SearchMyLogsResponse(
        @Schema(description = "검색 기록 목록")
        List<LogItem> logs
) {

    @Schema(description = "검색 기록 항목")
    public record LogItem(
            @Schema(description = "검색 기록 ID", example = "abc123")
            String id,
            @Schema(description = "검색어", example = "야구")
            String keyword
    ) {}

    public static SearchMyLogsResponse from(SearchMyLogResult result) {
        List<LogItem> items = result.logs().stream()
                .map(item -> new LogItem(
                        item.id(),
                        item.keyword()
                ))
                .toList();

        return new SearchMyLogsResponse(items);
    }

}
