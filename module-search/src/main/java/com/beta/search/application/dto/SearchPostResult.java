package com.beta.search.application.dto;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.PostInfo;
import com.beta.search.domain.document.PostDocument;

import java.time.LocalDateTime;
import java.util.List;

public record SearchPostResult(
        List<SearchPostItem> posts,
        boolean hasNext
) {

    public record SearchPostItem(
            Long postId,
            String snippet,
            AuthorInfo author,
            String channel,
            List<String> imageUrls,
            List<String> hashtags,
            Integer commentCount,
            Integer likeCount,
            Integer sadCount,
            Integer funCount,
            Integer hypeCount,
            Boolean hasLiked,
            LocalDateTime createdAt
    ) {
        public static List<SearchPostItem> from(List<Hit<PostDocument>> hits) {
            return hits.stream()
                    .map(hit -> {
                        Long postId = hit.source() != null ? hit.source().getId() : null;
                        String snippet = extractSnippet(hit);
                        return new SearchPostItem(postId, snippet, null, null, null, null, null, null, null, null, null, null, null);
                    })
                    .toList();
        }

        public SearchPostItem enrichWithPostInfo(PostInfo postInfo) {
            if (postInfo == null) {
                return this;
            }
            return new SearchPostItem(
                    this.postId,
                    this.snippet,
                    postInfo.getAuthor(),
                    postInfo.getChannel(),
                    postInfo.getImageUrls(),
                    postInfo.getHashtags(),
                    postInfo.getCommentCount(),
                    postInfo.getLikeCount(),
                    postInfo.getSadCount(),
                    postInfo.getFunCount(),
                    postInfo.getHypeCount(),
                    postInfo.getHasLiked(),
                    postInfo.getCreatedAt()
            );
        }

        // 검색어가 hashtags나 authorNickname 에서만 매칭되면
        // content highlight가 없어 snippet 이 아닌 content 로 반환함
        private static String extractSnippet(Hit<PostDocument> hit) {
            if (hit.highlight() == null || hit.highlight().get("content") == null) {
                return hit.source() != null ? hit.source().getContent() : null;
            }
            List<String> highlights = hit.highlight().get("content");
            return highlights.isEmpty() ? null : highlights.getFirst();
        }
    }
}
