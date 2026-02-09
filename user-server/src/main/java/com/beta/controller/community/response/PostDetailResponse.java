package com.beta.controller.community.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 상세 응답")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시글 내용", example = "오늘 경기 너무 좋았다!")
    private String content;

    @Schema(description = "채널 (ALL: 전체, 팀코드: 팀 채널)", example = "DOOSAN")
    private String channel;

    @Schema(description = "이미지 목록 (id, url 포함)")
    private List<PostListResponse.ImageResponse> images;

    @Schema(description = "해시태그 목록", example = "[\"야구\", \"두산\"]")
    private List<String> hashtags;

    @Schema(description = "작성자 정보")
    private PostListResponse.AuthorInfo author;

    @Schema(description = "감정표현 카운트")
    private PostListResponse.EmotionCount emotions;

    @Schema(description = "댓글 수", example = "5")
    private Integer commentCount;

    @Schema(description = "작성일시", example = "2025-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "댓글 목록 (트리 구조)")
    private List<CommentResponse> comments;

    @Getter
    @Builder
    @Schema(description = "댓글 정보")
    public static class CommentResponse {

        @Schema(description = "댓글 ID", example = "1")
        private Long commentId;

        @Schema(description = "작성자 ID", example = "1")
        private Long userId;

        @Schema(description = "작성자 닉네임", example = "야구팬123")
        private String nickname;

        @Schema(description = "댓글 내용", example = "동감합니다!")
        private String content;

        @Schema(description = "좋아요 수", example = "5")
        private Integer likeCount;

        @Schema(description = "댓글 깊이 (0: 댓글, 1: 답글)", example = "0")
        private Integer depth;

        @Schema(description = "작성일시", example = "2025-01-01T12:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "답글 목록 (depth 0인 경우에만 존재)")
        private List<CommentResponse> replies;
    }
}
