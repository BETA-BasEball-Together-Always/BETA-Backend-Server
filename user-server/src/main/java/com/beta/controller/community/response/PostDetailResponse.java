package com.beta.controller.community.response;

import com.beta.community.application.dto.PostDetailDto;
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

    @Schema(description = "다음 댓글 페이지 존재 여부", example = "true")
    private boolean hasNextComments;

    @Schema(description = "다음 댓글 페이지 커서 (다음 페이지 조회 시 사용)", example = "20")
    private Long nextCommentCursor;

    @Getter
    @Builder
    @Schema(description = "댓글 정보")
    public static class CommentResponse {

        @Schema(description = "댓글 ID", example = "1")
        private Long commentId;

        @Schema(description = "작성자 ID (삭제된 댓글은 null)", example = "1")
        private Long userId;

        @Schema(description = "작성자 닉네임 (삭제된 댓글은 null)", example = "야구팬123")
        private String nickname;

        @Schema(description = "작성자 응원팀 코드 (삭제된 댓글은 null)", example = "DOOSAN")
        private String teamCode;

        @Schema(description = "댓글 내용 (삭제된 댓글은 '삭제된 댓글입니다')", example = "동감합니다!")
        private String content;

        @Schema(description = "좋아요 수", example = "5")
        private Integer likeCount;

        @Schema(description = "댓글 깊이 (0: 댓글, 1: 답글)", example = "0")
        private Integer depth;

        @Schema(description = "작성일시", example = "2025-01-01T12:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "내가 좋아요 눌렀는지 여부", example = "false")
        private boolean isLiked;

        @Schema(description = "삭제된 댓글인지 여부", example = "false")
        private boolean deleted;

        @Schema(description = "답글 목록 (depth 0인 경우에만 존재)")
        private List<ReplyResponse> replies;
    }

    @Getter
    @Builder
    @Schema(description = "답글 정보")
    public static class ReplyResponse {

        @Schema(description = "댓글 ID", example = "2")
        private Long commentId;

        @Schema(description = "작성자 ID (삭제된 댓글은 null)", example = "1")
        private Long userId;

        @Schema(description = "작성자 닉네임 (삭제된 댓글은 null)", example = "야구팬456")
        private String nickname;

        @Schema(description = "작성자 응원팀 코드 (삭제된 댓글은 null)", example = "DOOSAN")
        private String teamCode;

        @Schema(description = "댓글 내용 (삭제된 댓글은 '삭제된 댓글입니다')", example = "저도요!")
        private String content;

        @Schema(description = "좋아요 수", example = "3")
        private Integer likeCount;

        @Schema(description = "댓글 깊이 (답글은 항상 1)", example = "1")
        private Integer depth;

        @Schema(description = "작성일시", example = "2025-01-01T12:35:00")
        private LocalDateTime createdAt;

        @Schema(description = "내가 좋아요 눌렀는지 여부", example = "false")
        private boolean isLiked;

        @Schema(description = "삭제된 댓글인지 여부", example = "false")
        private boolean deleted;
    }

    public static PostDetailResponse from(PostDetailDto dto) {
        return PostDetailResponse.builder()
                .postId(dto.getPostId())
                .content(dto.getContent())
                .channel(dto.getChannel())
                .images(dto.getImages().stream()
                        .map(PostListResponse.ImageResponse::from)
                        .toList())
                .hashtags(dto.getHashtags())
                .author(PostListResponse.AuthorInfo.builder()
                        .userId(dto.getAuthor().getUserId())
                        .nickname(dto.getAuthor().getNickname())
                        .teamCode(dto.getAuthor().getTeamCode())
                        .build())
                .emotions(PostListResponse.EmotionCount.builder()
                        .likeCount(dto.getLikeCount())
                        .sadCount(dto.getSadCount())
                        .funCount(dto.getFunCount())
                        .hypeCount(dto.getHypeCount())
                        .build())
                .commentCount(dto.getCommentCount())
                .createdAt(dto.getCreatedAt())
                .comments(toCommentResponses(dto.getComments()))
                .hasNextComments(dto.isHasNextComments())
                .nextCommentCursor(dto.getNextCommentCursor())
                .build();
    }

    private static List<CommentResponse> toCommentResponses(List<PostDetailDto.CommentDto> comments) {
        if (comments == null) {
            return List.of();
        }
        return comments.stream()
                .map(PostDetailResponse::toCommentResponse)
                .toList();
    }

    private static CommentResponse toCommentResponse(PostDetailDto.CommentDto dto) {
        return CommentResponse.builder()
                .commentId(dto.getCommentId())
                .userId(dto.getUserId())
                .nickname(dto.getNickname())
                .teamCode(dto.getTeamCode())
                .content(dto.getContent())
                .likeCount(dto.getLikeCount())
                .depth(dto.getDepth())
                .createdAt(dto.getCreatedAt())
                .isLiked(dto.isLiked())
                .deleted(dto.isDeleted())
                .replies(toReplyResponses(dto.getReplies()))
                .build();
    }

    private static List<ReplyResponse> toReplyResponses(List<PostDetailDto.CommentDto> replies) {
        if (replies == null) {
            return List.of();
        }
        return replies.stream()
                .map(PostDetailResponse::toReplyResponse)
                .toList();
    }

    private static ReplyResponse toReplyResponse(PostDetailDto.CommentDto dto) {
        return ReplyResponse.builder()
                .commentId(dto.getCommentId())
                .userId(dto.getUserId())
                .nickname(dto.getNickname())
                .teamCode(dto.getTeamCode())
                .content(dto.getContent())
                .likeCount(dto.getLikeCount())
                .depth(dto.getDepth())
                .createdAt(dto.getCreatedAt())
                .isLiked(dto.isLiked())
                .deleted(dto.isDeleted())
                .build();
    }
}
