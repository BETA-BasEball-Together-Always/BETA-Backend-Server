package com.beta.community.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionToggleDto {

    private Long postId;
    private String emotionType;
    private boolean toggled;
    private int likeCount;
    private int sadCount;
    private int funCount;
    private int hypeCount;
}
