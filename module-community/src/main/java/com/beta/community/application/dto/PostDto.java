package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDto {
    private Long postId;
    private Long userId;
    private String content;
    private String channel;
    private List<String> imageUrls;
    private List<String> hashtags;
    private String status;
    private LocalDateTime createdAt;
}
