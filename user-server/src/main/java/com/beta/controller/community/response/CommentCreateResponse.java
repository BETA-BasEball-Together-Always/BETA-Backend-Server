package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentCreateResponse {

    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;
    private Integer depth;
    private LocalDateTime createdAt;
}
