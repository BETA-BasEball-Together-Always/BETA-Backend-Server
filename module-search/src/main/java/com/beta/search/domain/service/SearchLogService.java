package com.beta.search.domain.service;

import com.beta.search.domain.document.SearchLogDocument;
import com.beta.search.infra.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private static final int DEFAULT_SIZE = 3;

    private final SearchLogRepository searchLogRepository;

    public void save(String keyword, Long userId, String searchType, int resultCount) {
        SearchLogDocument log = SearchLogDocument.create(
                keyword,
                userId,
                searchType,
                resultCount
        );
        searchLogRepository.save(log);
    }

    public List<String> findRecentKeywordsByUser(Long userId) {
        return searchLogRepository.findRecentKeywordsByUser(userId, DEFAULT_SIZE);
    }

    public List<String> searchWhileTyping(String keyword) {
        return searchLogRepository.searchByKeywordPrefix(keyword, DEFAULT_SIZE);
    }
}
