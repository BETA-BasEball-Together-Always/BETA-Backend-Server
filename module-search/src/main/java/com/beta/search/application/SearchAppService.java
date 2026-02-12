package com.beta.search.application;

import com.beta.search.application.dto.SearchMyLogResult;
import com.beta.search.application.dto.SearchMyLogResult.SearchMyLogItem;
import com.beta.search.domain.service.SearchLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchAppService {
    private static final int RECENT_KEYWORDS_SIZE = 5;

    private final SearchLogService searchLogService;

    public SearchMyLogResult findMyRecentKeywords(Long userId) {
        List<SearchMyLogItem> items = SearchMyLogItem.from(
                searchLogService.findRecentKeywordsByUser(userId, RECENT_KEYWORDS_SIZE)
        );
        return new SearchMyLogResult(items);
    }

    public void deleteMySearchLog(Long userId, String logId) {
        searchLogService.deleteByIdAndUserId(logId, userId);
    }
}
