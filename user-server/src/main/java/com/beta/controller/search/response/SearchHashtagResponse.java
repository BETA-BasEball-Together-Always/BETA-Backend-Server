package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchHashtagResult;

import java.util.List;

public record SearchHashtagResponse(
        List<HashtagItem> hashtags,
        boolean hasNext
) {

    public static SearchHashtagResponse from(SearchHashtagResult result) {
        List<HashtagItem> items = result.hashtags().stream()
                .map(item -> new HashtagItem(
                        item.id(),
                        item.tagName(),
                        item.usageCount()
                ))
                .toList();

        return new SearchHashtagResponse(items, result.hasNext());
    }

    public record HashtagItem(
            Long hashtagId,
            String tagName,
            Long usageCount
    ) {}
}
