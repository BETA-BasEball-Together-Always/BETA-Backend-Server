package com.beta.search.application;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.core.port.PostPort;
import com.beta.core.port.dto.PostInfo;
import com.beta.search.application.dto.*;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.document.UserDocument;
import com.beta.search.domain.service.SearchHashtagService;
import com.beta.search.domain.service.SearchLogService;
import com.beta.search.domain.service.SearchPostService;
import com.beta.search.domain.service.SearchUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchFacadeService {
    private static final int PAGE_SIZE = 10;
    private static final int WHILE_TYPING_LOG_SIZE = 3;
    private static final int WHILE_TYPING_USER_SIZE = 10;

    private final SearchLogService searchLogService;
    private final SearchUserService searchUserService;
    private final SearchPostService searchPostService;
    private final SearchHashtagService searchHashtagService;
    private final PostPort postPort;

    public SearchSuggestionsResult searchWhileTyping(String keyword) {
        List<String> suggestedKeywords =
                searchLogService.searchWhileTyping(keyword, WHILE_TYPING_LOG_SIZE);

        List<SearchSuggestionsResult.SuggestedUser> suggestedUsers =
                SearchSuggestionsResult.SuggestedUser.from(
                        searchUserService.searchWhileTyping(keyword, WHILE_TYPING_USER_SIZE)
                );

        return new SearchSuggestionsResult(
                suggestedKeywords,
                suggestedUsers
        );
    }

    public SearchPostResult searchPosts(String keyword, String channel, Long userId, SearchCursor cursor) {
        List<Hit<PostDocument>> hits = new ArrayList<>(
                searchPostService.searchInChannel(keyword, channel, cursor, PAGE_SIZE + 1)
        );

        boolean hasNext = trimToPageSize(hits);

        List<SearchPostResult.SearchPostItem> searchItems = SearchPostResult.SearchPostItem.from(hits);

        List<Long> postIds = searchItems.stream()
                .map(SearchPostResult.SearchPostItem::postId)
                .toList();
        Map<Long, PostInfo> postInfoMap = postPort.findPostsByIds(postIds, userId);

        List<SearchPostResult.SearchPostItem> enrichedPostItems = searchItems.stream()
                .filter(item -> postInfoMap.containsKey(item.postId()))
                .map(item -> item.enrichWithPostInfo(postInfoMap.get(item.postId())))
                .toList();

        searchLogService.save(keyword, userId, SearchType.POST.name());

        return new SearchPostResult(enrichedPostItems, hasNext);
    }

    public SearchUserResult searchUsers(String keyword, Long userId, SearchCursor cursor) {
        List<UserDocument> users = searchUserService.search(keyword, cursor, PAGE_SIZE + 1);

        boolean hasNext = trimToPageSize(users);

        searchLogService.save(keyword, userId, SearchType.USER.name());

        return new SearchUserResult(
                SearchUserResult.SearchUserItem.from(users),
                hasNext
        );
    }

    public SearchHashtagResult searchHashtags(String keyword, Long userId, SearchCursor cursor) {
        List<HashtagDocument> hashtags = searchHashtagService.search(keyword, cursor, PAGE_SIZE + 1);

        boolean hasNext = trimToPageSize(hashtags);

        searchLogService.save(keyword, userId, SearchType.HASHTAG.name());

        return new SearchHashtagResult(
                SearchHashtagResult.SearchHashtagItem.from(hashtags),
                hasNext
        );
    }

    private <T> boolean trimToPageSize(List<T> items) {
        if (items.size() <= PAGE_SIZE) {
            return false;
        }
        items.subList(PAGE_SIZE, items.size()).clear();
        return true;
    }

}
