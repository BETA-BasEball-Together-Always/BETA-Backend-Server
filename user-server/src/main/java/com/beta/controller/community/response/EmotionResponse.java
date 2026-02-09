package com.beta.controller.community.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "감정표현 토글 응답")
public class EmotionResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "토글된 감정표현 타입", example = "LIKE")
    private String emotionType;

    @Schema(description = "토글 결과 (true: 추가됨, false: 제거됨)", example = "true")
    private boolean toggled;

    @Schema(description = "현재 감정표현 카운트")
    private PostListResponse.EmotionCount emotions;
}
