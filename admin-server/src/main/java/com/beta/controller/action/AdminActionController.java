package com.beta.controller.action;

import com.beta.application.action.AdminActionFacadeService;
import com.beta.controller.action.request.AdminActionRequest;
import com.beta.controller.action.response.AdminActionResponse;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Actions", description = "관리자 조치 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminActionController {

    private final AdminActionFacadeService adminActionFacadeService;

    @Operation(summary = "회원 정지")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정지 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 수행 불가능한 액션",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "유효성 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "액션 불가", value = """
                                            {"code": "ADMIN002", "message": "수행할 수 없는 관리자 액션입니다.", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "ADMIN001", "message": "관리자 권한의 사용자가 아닙니다.", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PatchMapping("/members/{memberId}/suspend")
    public ResponseEntity<AdminActionResponse> suspendMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminActionFacadeService.suspendMember(userDetails.userId(), memberId, request.reason());
        return ResponseEntity.ok(AdminActionResponse.success("회원이 정지되었습니다."));
    }

    @Operation(summary = "회원 정지 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정지 해제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 수행 불가능한 액션",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/members/{memberId}/unsuspend")
    public ResponseEntity<AdminActionResponse> unsuspendMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminActionFacadeService.unsuspendMember(userDetails.userId(), memberId, request.reason());
        return ResponseEntity.ok(AdminActionResponse.success("회원 정지가 해제되었습니다."));
    }

    @Operation(summary = "게시글 숨김")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "숨김 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 수행 불가능한 액션",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
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
    @PatchMapping("/posts/{postId}/hide")
    public ResponseEntity<AdminActionResponse> hidePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminActionFacadeService.hidePost(userDetails.userId(), postId, request.reason());
        return ResponseEntity.ok(AdminActionResponse.success("게시글이 숨김 처리되었습니다."));
    }

    @Operation(summary = "게시글 다시 노출")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다시 노출 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 수행 불가능한 액션",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
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
    @PatchMapping("/posts/{postId}/unhide")
    public ResponseEntity<AdminActionResponse> unhidePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminActionFacadeService.unhidePost(userDetails.userId(), postId, request.reason());
        return ResponseEntity.ok(AdminActionResponse.success("게시글 숨김이 해제되었습니다."));
    }

    @Operation(summary = "댓글 숨김")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "숨김 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 수행 불가능한 액션",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/comments/{commentId}/hide")
    public ResponseEntity<AdminActionResponse> hideComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminActionFacadeService.hideComment(userDetails.userId(), commentId, request.reason());
        return ResponseEntity.ok(AdminActionResponse.success("댓글이 숨김 처리되었습니다."));
    }

    @Operation(summary = "댓글 다시 노출")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다시 노출 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 수행 불가능한 액션",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/comments/{commentId}/unhide")
    public ResponseEntity<AdminActionResponse> unhideComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminActionFacadeService.unhideComment(userDetails.userId(), commentId, request.reason());
        return ResponseEntity.ok(AdminActionResponse.success("댓글 숨김이 해제되었습니다."));
    }
}
