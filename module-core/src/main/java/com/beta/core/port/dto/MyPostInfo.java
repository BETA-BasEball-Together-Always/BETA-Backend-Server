package com.beta.core.port.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyPostInfo {

    private final Long postId;
    private final AuthorInfo author;
    private final String content;
    private final String channel;
    private final List<ImageInfo> images;
    private final List<String> hashtags;
    private final Integer likeCount;
    private final Integer sadCount;
    private final Integer funCount;
    private final Integer hypeCount;
    private final Integer commentCount;
    private final LocalDateTime createdAt;

    @Getter
    @Builder
    public static class ImageInfo {
        private final Long imageId;
        private final String imageUrl;
    }
}
