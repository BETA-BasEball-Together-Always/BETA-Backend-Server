package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentLikeResponse {

    private Long commentId;
    private boolean liked;
    private Integer likeCount;
}
