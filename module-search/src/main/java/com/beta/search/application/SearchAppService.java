package com.beta.search.application;

import com.beta.search.domain.service.SearchLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchAppService {
    private static final int RECENT_KEYWORDS_SIZE = 5;

    private final SearchLogService searchLogService;

    public List<String> findMyRecentKeywords(Long userId) {
        return searchLogService.findRecentKeywordsByUser(userId, RECENT_KEYWORDS_SIZE);
    }
}
