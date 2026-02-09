package com.beta.controller.community.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 차단 응답")
public class BlockResponse {

    @Schema(description = "차단 요청자 ID", example = "1")
    private Long userId;

    @Schema(description = "차단 대상 ID", example = "2")
    private Long blockedUserId;

    @Schema(description = "차단 상태 (true: 차단됨, false: 차단 해제됨)", example = "true")
    private boolean blocked;
}
