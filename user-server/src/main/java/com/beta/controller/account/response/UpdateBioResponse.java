package com.beta.controller.account.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "한줄 소개 수정 응답")
public class UpdateBioResponse {

    @Schema(description = "수정된 한줄 소개 (삭제 시 null)", example = "야구 좋아하는 두산 팬입니다")
    private String bio;

    @Schema(description = "처리 메시지", example = "한줄 소개가 수정되었습니다")
    private String message;

    public static UpdateBioResponse of(String bio) {
        return UpdateBioResponse.builder()
                .bio(bio)
                .message("한줄 소개가 수정되었습니다")
                .build();
    }
}
