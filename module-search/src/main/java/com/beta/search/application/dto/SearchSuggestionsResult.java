package com.beta.search.application.dto;

import com.beta.search.domain.document.UserDocument;

import java.util.List;

public record SearchSuggestionsResult(
        List<String> suggestedKeywords,
        List<SuggestedUser> suggestedUsers
) {

    public record SuggestedUser(
            Long userId,
            String nickname,
            String bio,
            String teamCode,
            String teamNameKr
    ) {
        public static List<SuggestedUser> from(List<UserDocument> docs) {
            return docs.stream()
                    .map(doc -> new SuggestedUser(
                            doc.getId(),
                            doc.getNickname(),
                            doc.getBio(),
                            doc.getTeamCode(),
                            doc.getTeamNameKr()
                    ))
                    .toList();
        }
    }
}
