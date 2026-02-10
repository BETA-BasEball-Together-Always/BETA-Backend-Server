package com.beta.controller.search.response;

import com.beta.core.port.dto.AuthorInfo;
import com.beta.search.application.dto.SearchPostResult;

import java.time.LocalDateTime;
import java.util.List;

public record SearchPostResponse(
        List<PostItem> posts,
        boolean hasNext
) {

    public static SearchPostResponse from(SearchPostResult result) {
        List<PostItem> items = result.posts().stream()
                .map(PostItem::from)
                .toList();

        return new SearchPostResponse(items, result.hasNext());
    }

    public record PostItem(
            Long postId,
            String snippet,
            String channel,
            List<String> imageUrls,
            List<String> hashtags,
            Author author,
            EmotionCount emotions,
            Integer commentCount,
            Boolean hasLiked,
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

    public record Author(
            Long userId,
            String nickname,
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

    public record EmotionCount(
            Integer likeCount,
            Integer sadCount,
            Integer funCount,
            Integer hypeCount
    ) {}
}
