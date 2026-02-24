package com.beta.controller.account.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "계정 탈퇴 응답")
public class WithdrawResponse {

    @Schema(description = "처리 메시지", example = "계정 탈퇴 요청이 처리되었습니다. 30일 후 모든 데이터가 삭제됩니다.")
    private String message;

    @Schema(description = "탈퇴 요청 시간", example = "2025-01-15T10:30:00")
    private LocalDateTime withdrawnAt;

    @Schema(description = "데이터 삭제 예정 시간 (탈퇴 요청 30일 후)", example = "2025-02-14T10:30:00")
    private LocalDateTime scheduledDeletionAt;

    public static WithdrawResponse of(LocalDateTime withdrawnAt) {
        return WithdrawResponse.builder()
                .message("계정 탈퇴 요청이 처리되었습니다. 30일 후 모든 데이터가 삭제됩니다.")
                .withdrawnAt(withdrawnAt)
                .scheduledDeletionAt(withdrawnAt.plusDays(30))
                .build();
    }
}
