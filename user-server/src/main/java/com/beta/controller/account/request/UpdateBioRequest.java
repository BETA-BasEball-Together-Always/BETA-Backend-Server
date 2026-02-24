package com.beta.controller.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "한줄 소개 수정 요청")
public class UpdateBioRequest {

    @Schema(description = "한줄 소개 (빈 문자열이면 삭제)", example = "야구 좋아하는 두산 팬입니다", maxLength = 50)
    @Size(max = 50, message = "한줄 소개는 50자 이하여야 합니다")
    private String bio;
}
