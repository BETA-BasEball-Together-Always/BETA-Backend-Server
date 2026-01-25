package com.beta.controller.response;

import com.beta.community.application.dto.PostDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CreatePostResponse {
    private Long postId;
    private Long userId;
    private String content;
    private String channel;
    private List<String> imageUrls;
    private List<String> hashtags;
    private String status;
    private LocalDateTime createdAt;

    public static CreatePostResponse from(PostDto postDto) {
        return CreatePostResponse.builder()
                .postId(postDto.getPostId())
                .userId(postDto.getUserId())
                .content(postDto.getContent())
                .channel(postDto.getChannel())
                .imageUrls(postDto.getImageUrls())
                .hashtags(postDto.getHashtags())
                .status(postDto.getStatus())
                .createdAt(postDto.getCreatedAt())
                .build();
    }
}
