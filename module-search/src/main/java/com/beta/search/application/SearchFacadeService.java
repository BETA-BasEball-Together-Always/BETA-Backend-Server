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
        SearchCursor nextCursor = extractNextCursor(hits, hasNext);

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

        return new SearchPostResult(enrichedPostItems, hasNext, nextCursor);
    }

    public SearchUserResult searchUsers(String keyword, Long userId, SearchCursor cursor) {
        List<org.springframework.data.elasticsearch.core.SearchHit<UserDocument>> userHits =
                new ArrayList<>(searchUserService.search(keyword, cursor, PAGE_SIZE + 1));

        boolean hasNext = trimToPageSize(userHits);
        SearchCursor nextCursor = extractNextCursorFromUserHits(userHits, hasNext);

        List<UserDocument> users = userHits.stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .toList();

        searchLogService.save(keyword, userId, SearchType.USER.name());

        return new SearchUserResult(
                SearchUserResult.SearchUserItem.from(users),
                hasNext,
                nextCursor
        );
    }

    public SearchHashtagResult searchHashtags(String keyword, Long userId, SearchCursor cursor) {
        List<org.springframework.data.elasticsearch.core.SearchHit<HashtagDocument>> hashtagHits =
                new ArrayList<>(searchHashtagService.search(keyword, cursor, PAGE_SIZE + 1));

        boolean hasNext = trimToPageSize(hashtagHits);
        SearchCursor nextCursor = extractNextCursorFromHashtagHits(hashtagHits, hasNext);

        List<HashtagDocument> hashtags = hashtagHits.stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .toList();

        searchLogService.save(keyword, userId, SearchType.HASHTAG.name());

        return new SearchHashtagResult(
                SearchHashtagResult.SearchHashtagItem.from(hashtags),
                hasNext,
                nextCursor
        );
    }

    private <T> boolean trimToPageSize(List<T> items) {
        if (items.size() <= PAGE_SIZE) {
            return false;
        }
        items.subList(PAGE_SIZE, items.size()).clear();
        return true;
    }

    private SearchCursor extractNextCursor(List<Hit<PostDocument>> hits, boolean hasNext) {
        if (!hasNext || hits.isEmpty()) {
            return null;
        }

        Hit<PostDocument> lastHit = hits.getLast();
        if (lastHit.score() == null || lastHit.source() == null || lastHit.source().getId() == null) {
            return null;
        }

        return SearchCursor.of(lastHit.score().floatValue(), lastHit.source().getId());
    }

    private SearchCursor extractNextCursorFromUserHits(
            List<org.springframework.data.elasticsearch.core.SearchHit<UserDocument>> hits,
            boolean hasNext
    ) {
        if (!hasNext || hits.isEmpty()) {
            return null;
        }

        org.springframework.data.elasticsearch.core.SearchHit<UserDocument> lastHit = hits.getLast();
        if (lastHit.getContent() == null || lastHit.getContent().getId() == null) {
            return null;
        }

        return SearchCursor.of(lastHit.getScore(), lastHit.getContent().getId());
    }

    private SearchCursor extractNextCursorFromHashtagHits(
            List<org.springframework.data.elasticsearch.core.SearchHit<HashtagDocument>> hits,
            boolean hasNext
    ) {
        if (!hasNext || hits.isEmpty()) {
            return null;
        }

        org.springframework.data.elasticsearch.core.SearchHit<HashtagDocument> lastHit = hits.getLast();
        if (lastHit.getContent() == null || lastHit.getContent().getId() == null) {
            return null;
        }

        return SearchCursor.of(lastHit.getScore(), lastHit.getContent().getId());
    }

}
