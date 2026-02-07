package com.beta.search.domain.service;

import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.infra.repository.SearchPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchPostService {

    private final SearchPostRepository searchPostRepository;

    public List<SearchHit<PostDocument>> search(String keyword, SearchCursor cursor, int size) {
        return searchPostRepository.searchByKeyword(keyword, cursor, size);
    }

    public List<SearchHit<PostDocument>> searchInChannel(String keyword, String channel, SearchCursor cursor, int size) {
        return searchPostRepository.searchByKeywordAndChannel(keyword, channel, cursor, size);
    }
}
