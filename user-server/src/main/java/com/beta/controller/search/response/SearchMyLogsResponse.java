package com.beta.controller.search.response;

import java.util.List;

public record SearchMyLogsResponse(
        List<String> keywords
) {

    public static SearchMyLogsResponse of(List<String> keywords) {
        return new SearchMyLogsResponse(keywords);
    }
}
