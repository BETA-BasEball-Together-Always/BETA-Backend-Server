package com.beta.search.application.dto;

import com.beta.search.domain.document.SearchLogDocument;

import java.util.List;

public record SearchMyLogResult(
        List<SearchMyLogItem> logs
) {
    public record SearchMyLogItem(
            String id,
            String keyword
    ) {
        public static List<SearchMyLogItem> from(List<SearchLogDocument> docs) {
            return docs.stream()
                    .map(doc -> new SearchMyLogItem(
                            doc.getId(),
                            doc.getKeyword()
                    ))
                    .toList();
        }
    }
}
