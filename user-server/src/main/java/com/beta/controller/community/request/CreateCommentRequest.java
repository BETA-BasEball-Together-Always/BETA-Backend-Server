package com.beta.controller.community.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "댓글/답글 작성 요청")
public class CreateCommentRequest {

    @Schema(description = "댓글 내용 (1~500자)", example = "동감합니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(min = 1, max = 500, message = "댓글은 1~500자 사이여야 합니다")
    private String content;

    @Schema(description = "부모 댓글 ID (답글인 경우에만 입력, null이면 최상위 댓글)", example = "1")
    private Long parentId;
}
