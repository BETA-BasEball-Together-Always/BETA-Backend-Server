package com.beta.controller.community.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmotionRequest {

    @NotNull(message = "감정표현 타입은 필수입니다")
    @Pattern(regexp = "^(LIKE|SAD|FUN|HYPE)$", message = "감정표현은 LIKE, SAD, FUN, HYPE만 가능합니다")
    private String emotionType;
}
