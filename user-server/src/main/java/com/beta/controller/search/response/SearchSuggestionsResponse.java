package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchSuggestionsResult;

import java.util.List;

public record SearchSuggestionsResponse(
        List<String> suggestedKeywords,
        List<SuggestedUser> suggestedUsers
) {

    public static SearchSuggestionsResponse from(SearchSuggestionsResult result) {
        List<SuggestedUser> users = result.suggestedUsers().stream()
                .map(u -> new SuggestedUser(
                        u.userId(),
                        u.nickname(),
                        u.bio(),
                        u.teamCode(),
                        u.teamNameKr()
                ))
                .toList();

        return new SearchSuggestionsResponse(
                result.suggestedKeywords(),
                users
        );
    }

    public record SuggestedUser(
            Long userId,
            String nickname,
            String bio,
            String teamCode,
            String teamNameKr
    ) {}
}
