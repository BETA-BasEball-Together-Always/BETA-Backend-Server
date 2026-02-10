package com.beta.core.port.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostInfo {

    private final Long id;
    private final AuthorInfo author;
    private final String channel;
    private final List<String> imageUrls;
    private final List<String> hashtags;
    private final Integer commentCount;
    private final Integer likeCount;
    private final Integer sadCount;
    private final Integer funCount;
    private final Integer hypeCount;
    private final Boolean hasLiked;
    private final LocalDateTime createdAt;
}
