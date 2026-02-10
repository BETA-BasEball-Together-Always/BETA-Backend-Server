package com.beta.search.application.dto;

import com.beta.search.domain.document.UserDocument;

import java.util.List;

public record SearchUserResult(
        List<SearchUserItem> users,
        boolean hasNext
) {
    public record SearchUserItem(
            Long userId,
            String nickname,
            String bio,
            String teamCode,
            String teamNameKr
    ) {
        public static List<SearchUserItem> from(List<UserDocument> docs) {
            return docs.stream()
                    .map(doc -> new SearchUserItem(
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
