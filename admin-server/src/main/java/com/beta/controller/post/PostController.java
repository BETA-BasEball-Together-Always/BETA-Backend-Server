package com.beta.controller.post;

import com.beta.application.post.AdminPostQueryFacadeService;
import com.beta.community.application.admin.AdminPostDetailFacadeService;
import com.beta.community.application.admin.dto.AdminPostCommentsResult;
import com.beta.community.application.admin.dto.AdminPostDetailResult;
import com.beta.community.application.admin.dto.AdminPostQueryResult;
import com.beta.controller.common.request.AdminPageRequest;
import com.beta.controller.common.response.AdminPageResponse;
import com.beta.controller.post.request.AdminPostSearchRequest;
import com.beta.controller.post.response.AdminPostCommentsResponse;
import com.beta.controller.post.response.AdminPostDetailResponse;
import com.beta.controller.post.response.AdminPostItemResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Posts", description = "관리자 게시글 관리 조회 API")
@RestController
@RequestMapping("/api/v1/admin/posts")
@RequiredArgsConstructor
public class PostController {

    private final AdminPostQueryFacadeService adminPostQueryFacadeService;
    private final AdminPostDetailFacadeService adminPostDetailFacadeService;

    @Operation(summary = "관리자 게시글 목록 조회")
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
    public ResponseEntity<AdminPageResponse<AdminPostItemResponse>> getPosts(
            @Valid @ModelAttribute AdminPageRequest pageRequest,
            @Valid @ModelAttribute AdminPostSearchRequest request
    ) {
        Page<AdminPostQueryResult> result = adminPostQueryFacadeService.getPosts(
                pageRequest.pageOrDefault(),
                pageRequest.sizeOrDefault(),
                request.status(),
                request.channel(),
                request.keywordOrNull()
        );

        Page<AdminPostItemResponse> responsePage = result.map(AdminPostItemResponse::from);
        return ResponseEntity.ok(AdminPageResponse.from(responsePage));
    }

    @Operation(summary = "관리자 게시글 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminPostDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<AdminPostDetailResponse> getPostDetail(@PathVariable Long postId) {
        AdminPostDetailResult result = adminPostDetailFacadeService.getPostDetail(postId);
        return ResponseEntity.ok(AdminPostDetailResponse.from(result));
    }

    @Operation(summary = "관리자 게시글 댓글 추가 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminPostCommentsResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{postId}/comments")
    public ResponseEntity<AdminPostCommentsResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor
    ) {
        AdminPostCommentsResult result = adminPostDetailFacadeService.getComments(postId, cursor);
        return ResponseEntity.ok(AdminPostCommentsResponse.from(result));
    }
}
