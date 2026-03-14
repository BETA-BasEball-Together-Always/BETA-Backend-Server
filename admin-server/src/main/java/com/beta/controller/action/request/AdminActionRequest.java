package com.beta.controller.action.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 액션 요청")
public record AdminActionRequest(
        @Schema(description = "조치 사유", example = "운영 정책 위반", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "사유는 필수입니다.")
        String reason
) {
}
