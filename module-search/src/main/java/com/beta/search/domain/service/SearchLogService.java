package com.beta.search.domain.service;

import com.beta.search.domain.document.SearchLogDocument;
import com.beta.search.infra.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    public void save(String keyword, Long userId, String searchType) {
        SearchLogDocument log = SearchLogDocument.create(keyword, userId, searchType);
        searchLogRepository.save(log);
    }

    public List<String> findRecentKeywordsByUser(Long userId, int size) {
        return searchLogRepository.findRecentKeywordsByUser(userId, size);
    }

    public List<String> searchWhileTyping(String keyword, int size) {
        return searchLogRepository.searchByKeywordPrefix(keyword, size);
    }
}
