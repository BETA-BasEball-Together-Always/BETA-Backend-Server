package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentCreateDto {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;
    private Integer depth;
    private LocalDateTime createdAt;
}
