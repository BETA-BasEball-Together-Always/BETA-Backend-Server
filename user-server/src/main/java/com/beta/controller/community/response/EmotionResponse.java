package com.beta.controller.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionResponse {

    private Long postId;
    private String emotionType;
    private boolean toggled;
    private PostListResponse.EmotionCount emotions;
}
