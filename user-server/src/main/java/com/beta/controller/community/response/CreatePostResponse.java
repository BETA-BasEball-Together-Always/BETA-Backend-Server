package com.beta.controller.community.response;

import com.beta.community.application.dto.PostDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 작성/수정 응답")
public class CreatePostResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "작성자 ID", example = "1")
    private Long userId;

    @Schema(description = "게시글 내용", example = "오늘 경기 너무 좋았다!")
    private String content;

    @Schema(description = "채널", example = "DOOSAN")
    private String channel;

    @Schema(description = "이미지 URL 목록", example = "[\"https://...\"]")
    private List<String> imageUrls;

    @Schema(description = "해시태그 목록", example = "[\"야구\", \"두산\"]")
    private List<String> hashtags;

    @Schema(description = "게시글 상태", example = "ACTIVE")
    private String status;

    @Schema(description = "작성일시", example = "2025-01-01T12:00:00")
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
