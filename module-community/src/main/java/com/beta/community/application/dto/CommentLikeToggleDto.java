package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentLikeToggleDto {
    private Long commentId;
    private boolean liked;
    private Integer likeCount;
}
