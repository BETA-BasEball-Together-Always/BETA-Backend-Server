package com.beta.search.application.dto;

import com.beta.search.domain.document.HashtagDocument;

import java.util.List;

public record SearchHashtagResult(
        List<SearchHashtagItem> hashtags,
        boolean hasNext
) {
    public record SearchHashtagItem(
            Long id,
            String tagName,
            Long usageCount
    ) {
        public static List<SearchHashtagItem> from(List<HashtagDocument> docs) {
            return docs.stream()
                    .map(doc -> new SearchHashtagItem(
                            doc.getId(),
                            doc.getTagName(),
                            doc.getUsageCount()
                    ))
                    .toList();
        }
    }
}
