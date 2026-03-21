package com.beta.controller.action.request;

import com.beta.domain.entity.AdminLogAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "관리자 로그 조회 요청")
public record AdminActionLogSearchRequest(
        @Schema(description = "관리자 액션 구분", example = "MEMBER_SUSPEND")
        AdminLogAction action,
        @Schema(description = "조회 시작일", example = "2026-03-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,
        @Schema(description = "조회 종료일", example = "2026-03-14")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
    @AssertTrue(message = "조회 시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isValidDateRange() {
        return from == null || to == null || !from.isAfter(to);
    }
}
