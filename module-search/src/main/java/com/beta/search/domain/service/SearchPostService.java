package com.beta.search.domain.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.infra.repository.SearchPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchPostService {

    private final SearchPostRepository searchPostRepository;

    public List<Hit<PostDocument>> searchInChannel(String keyword, String channel, SearchCursor cursor, int size) {
        return searchPostRepository.searchInChannel(keyword, channel, cursor, size);
    }
}
