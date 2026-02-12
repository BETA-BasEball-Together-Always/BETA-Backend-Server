package com.beta.controller.search.response;

import com.beta.search.application.dto.SearchHashtagResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "해시태그 검색 응답")
public record SearchHashtagResponse(
        @Schema(description = "검색된 해시태그 목록")
        List<HashtagItem> hashtags,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {

    @Schema(description = "해시태그 항목")
    public record HashtagItem(
            @Schema(description = "해시태그 ID", example = "1")
            Long hashtagId,
            @Schema(description = "해시태그명", example = "야구")
            String tagName,
            @Schema(description = "사용 횟수", example = "42")
            Long usageCount
    ) {}

    public static SearchHashtagResponse from(SearchHashtagResult result) {
        List<HashtagItem> items = result.hashtags().stream()
                .map(item -> new HashtagItem(
                        item.id(),
                        item.tagName(),
                        item.usageCount()
                ))
                .toList();

        return new SearchHashtagResponse(items, result.hasNext());
    }

}
