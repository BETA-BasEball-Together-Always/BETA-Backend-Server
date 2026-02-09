package com.beta.controller.community.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "댓글 작성 응답")
public class CommentCreateResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "작성자 ID", example = "1")
    private Long userId;

    @Schema(description = "댓글 내용", example = "동감합니다!")
    private String content;

    @Schema(description = "부모 댓글 ID (답글인 경우)", example = "null")
    private Long parentId;

    @Schema(description = "댓글 깊이 (0: 댓글, 1: 답글)", example = "0")
    private Integer depth;

    @Schema(description = "작성일시", example = "2025-01-01T12:30:00")
    private LocalDateTime createdAt;
}
