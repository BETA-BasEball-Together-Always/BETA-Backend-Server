package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchUserResult;

import java.util.List;

public record SearchUserResponse(
        List<UserItem> users,
        boolean hasNext
) {

    public static SearchUserResponse from(SearchUserResult result) {
        List<UserItem> items = result.users().stream()
                .map(item -> new UserItem(
                        item.userId(),
                        item.nickname(),
                        item.bio(),
                        item.teamCode(),
                        item.teamNameKr()
                ))
                .toList();

        return new SearchUserResponse(items, result.hasNext());
    }

    public record UserItem(
            Long userId,
            String nickname,
            String bio,
            String teamCode,
            String teamNameKr
    ) {}
}
