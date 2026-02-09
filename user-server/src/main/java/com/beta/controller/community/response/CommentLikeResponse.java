package com.beta.controller.community.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "댓글 좋아요 토글 응답")
public class CommentLikeResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "좋아요 상태 (true: 추가됨, false: 제거됨)", example = "true")
    private boolean liked;

    @Schema(description = "현재 좋아요 수", example = "5")
    private Integer likeCount;
}
