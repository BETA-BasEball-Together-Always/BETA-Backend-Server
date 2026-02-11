package com.beta.controller.community.response;

import com.beta.community.application.dto.CommentsDto;
import com.beta.community.application.dto.PostDetailDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "댓글 목록 응답")
public class CommentsResponse {

    @ArraySchema(arraySchema = @Schema(description = "댓글 목록 (트리 구조)"),
            schema = @Schema(implementation = PostDetailResponse.CommentResponse.class))
    private List<PostDetailResponse.CommentResponse> comments;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지 커서", example = "40")
    private Long nextCursor;

    public static CommentsResponse from(CommentsDto dto) {
        return CommentsResponse.builder()
                .comments(toCommentResponses(dto.getComments()))
                .hasNext(dto.isHasNext())
                .nextCursor(dto.getNextCursor())
                .build();
    }

    private static List<PostDetailResponse.CommentResponse> toCommentResponses(List<PostDetailDto.CommentDto> comments) {
        if (comments == null) {
            return List.of();
        }
        return comments.stream()
                .map(CommentsResponse::toCommentResponse)
                .toList();
    }

    private static PostDetailResponse.CommentResponse toCommentResponse(PostDetailDto.CommentDto dto) {
        return PostDetailResponse.CommentResponse.builder()
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
                .replies(toCommentResponses(dto.getReplies()))
                .build();
    }
}
