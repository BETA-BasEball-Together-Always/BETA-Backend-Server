package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchUserResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "계정 검색 응답")
public record SearchUserResponse(
        @Schema(description = "검색된 계정 목록")
        List<UserItem> users,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {

    @Schema(description = "계정 항목")
    public record UserItem(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,
            @Schema(description = "닉네임", example = "엘지팬1")
            String nickname,
            @Schema(description = "자기소개", example = "엘쥐짱")
            String bio,
            @Schema(description = "팀 코드", example = "LG")
            String teamCode,
            @Schema(description = "팀 한글명", example = "LG 트윈스")
            String teamNameKr
    ) {}

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

}
