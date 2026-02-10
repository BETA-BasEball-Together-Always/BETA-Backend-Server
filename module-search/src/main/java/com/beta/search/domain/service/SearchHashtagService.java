package com.beta.search.domain.service;

import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.infra.repository.SearchHashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHashtagService {

    private final SearchHashtagRepository searchHashtagRepository;

    public List<HashtagDocument> search(String keyword, SearchCursor cursor, int size) {
        return searchHashtagRepository.searchByKeyword(keyword, cursor, size)
                .stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
