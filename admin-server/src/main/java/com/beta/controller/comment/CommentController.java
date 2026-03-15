package com.beta.controller.comment;

import com.beta.application.comment.AdminCommentQueryFacadeService;
import com.beta.community.application.admin.dto.AdminCommentQueryResult;
import com.beta.controller.comment.request.AdminCommentSearchRequest;
import com.beta.controller.comment.response.AdminCommentItemResponse;
import com.beta.controller.common.request.AdminPageRequest;
import com.beta.controller.common.response.AdminPageResponse;
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

@Tag(name = "Admin Comments", description = "관리자 댓글 관리 조회 API")
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class CommentController {

    private final AdminCommentQueryFacadeService adminCommentQueryFacadeService;

    @Operation(summary = "관리자 댓글 목록 조회")
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
    public ResponseEntity<AdminPageResponse<AdminCommentItemResponse>> getComments(
            @Valid @ModelAttribute AdminPageRequest pageRequest,
            @Valid @ModelAttribute AdminCommentSearchRequest request
    ) {
        Page<AdminCommentQueryResult> result = adminCommentQueryFacadeService.getComments(
                pageRequest.pageOrDefault(),
                pageRequest.sizeOrDefault(),
                request.status(),
                request.keywordOrNull()
        );

        Page<AdminCommentItemResponse> responsePage = result.map(AdminCommentItemResponse::from);
        return ResponseEntity.ok(AdminPageResponse.from(responsePage));
    }
}
