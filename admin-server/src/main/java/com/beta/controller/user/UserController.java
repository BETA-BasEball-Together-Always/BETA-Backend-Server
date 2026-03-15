package com.beta.controller.user;

import com.beta.account.application.admin.AdminUserQueryAppService;
import com.beta.account.application.admin.dto.AdminUserQueryResult;
import com.beta.controller.common.request.AdminPageRequest;
import com.beta.controller.common.response.AdminPageResponse;
import com.beta.controller.user.request.AdminUserSearchRequest;
import com.beta.controller.user.response.AdminUserItemResponse;
import com.beta.core.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Users", description = "관리자 사용자 관리 조회 API")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final AdminUserQueryAppService adminUserQueryAppService;

    @Operation(summary = "관리자 사용자 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "ADMIN001", "message": "관리자 권한의 사용자가 아닙니다.", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping
    public ResponseEntity<AdminPageResponse<AdminUserItemResponse>> getUsers(
            @Valid @ModelAttribute AdminPageRequest pageRequest,
            @Valid @ModelAttribute AdminUserSearchRequest request
    ) {
        Page<AdminUserQueryResult> result = adminUserQueryAppService.getUsers(
                pageRequest.pageOrDefault(),
                pageRequest.sizeOrDefault(),
                request.status(),
                request.keywordOrNull()
        );

        Page<AdminUserItemResponse> responsePage = result.map(AdminUserItemResponse::from);
        return ResponseEntity.ok(AdminPageResponse.from(responsePage));
    }
}
