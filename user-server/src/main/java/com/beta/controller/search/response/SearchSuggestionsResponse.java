package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchSuggestionsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "검색어 추천 응답")
public record SearchSuggestionsResponse(
        @Schema(description = "추천 검색어 목록")
        List<String> suggestedKeywords,
        @Schema(description = "추천 계정 목록")
        List<SuggestedUser> suggestedUsers
) {

    @Schema(description = "추천 계정")
    public record SuggestedUser(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,
            @Schema(description = "닉네임", example = "야구팬")
            String nickname,
            @Schema(description = "자기소개", example = "야구팬입니다")
            String bio,
            @Schema(description = "팀 코드", example = "DOOSAN")
            String teamCode,
            @Schema(description = "팀 한글명", example = "두산 베어스")
            String teamNameKr
    ) {}

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

}
