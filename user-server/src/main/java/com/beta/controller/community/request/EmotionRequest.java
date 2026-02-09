package com.beta.controller.community.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "감정표현 토글 요청")
public class EmotionRequest {

    @Schema(description = "감정표현 타입 (LIKE: 좋아요, SAD: 슬퍼요, FUN: 웃겨요, HYPE: 열광해요)",
            example = "LIKE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "감정표현 타입은 필수입니다")
    @Pattern(regexp = "^(LIKE|SAD|FUN|HYPE)$", message = "감정표현은 LIKE, SAD, FUN, HYPE만 가능합니다")
    private String emotionType;
}
