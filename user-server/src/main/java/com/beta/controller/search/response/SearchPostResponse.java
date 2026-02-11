package com.beta.controller.search.response;

import com.beta.core.port.dto.AuthorInfo;
import com.beta.search.application.dto.SearchPostResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 검색 응답")
public record SearchPostResponse(
        @Schema(description = "검색된 게시글 목록")
        List<PostItem> posts,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {

    public static SearchPostResponse from(SearchPostResult result) {
        List<PostItem> items = result.posts().stream()
                .map(PostItem::from)
                .toList();

        return new SearchPostResponse(items, result.hasNext());
    }

    @Schema(description = "게시글 항목")
    public record PostItem(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,
            @Schema(description = "검색어 하이라이트 적용된 본문")
            String snippet,
            @Schema(description = "채널", example = "ALL")
            String channel,
            @Schema(description = "이미지 URL 목록")
            List<String> imageUrls,
            @Schema(description = "해시태그 목록")
            List<String> hashtags,
            @Schema(description = "작성자 정보")
            Author author,
            @Schema(description = "감정표현 수")
            EmotionCount emotions,
            @Schema(description = "댓글 수", example = "5")
            Integer commentCount,
            @Schema(description = "좋아요 여부")
            Boolean hasLiked,
            @Schema(description = "작성일시")
            LocalDateTime createdAt
    ) {
        public static PostItem from(SearchPostResult.SearchPostItem item) {
            return new PostItem(
                    item.postId(),
                    item.snippet(),
                    item.channel(),
                    item.imageUrls(),
                    item.hashtags(),
                    item.author() != null ? Author.from(item.author()) : null,
                    new EmotionCount(item.likeCount(), item.sadCount(), item.funCount(), item.hypeCount()),
                    item.commentCount(),
                    item.hasLiked(),
                    item.createdAt()
            );
        }
    }

    @Schema(description = "작성자 정보")
    public record Author(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,
            @Schema(description = "닉네임", example = "야구팬")
            String nickname,
            @Schema(description = "팀 코드", example = "LG")
            String teamCode
    ) {
        public static Author from(AuthorInfo author) {
            return new Author(
                    author.getUserId(),
                    author.getNickname(),
                    author.getTeamCode()
            );
        }
    }

    @Schema(description = "감정표현 수")
    public record EmotionCount(
            @Schema(description = "좋아요 수", example = "10")
            Integer likeCount,
            @Schema(description = "슬퍼요 수", example = "0")
            Integer sadCount,
            @Schema(description = "웃겨요 수", example = "3")
            Integer funCount,
            @Schema(description = "열광해요 수", example = "7")
            Integer hypeCount
    ) {}
}
