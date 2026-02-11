package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchMyLogResult;

import java.util.List;

public record SearchMyLogsResponse(
        List<LogItem> logs
) {

    public record LogItem(
            String id,
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
