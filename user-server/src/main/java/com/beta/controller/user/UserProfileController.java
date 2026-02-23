package com.beta.controller.user;

import com.beta.account.application.UserProfileAppService;
import com.beta.account.application.dto.UserProfilePostListDto;
import com.beta.controller.user.response.UserPostListResponse;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "사용자 프로필 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileAppService userProfileAppService;

    @Operation(summary = "사용자 게시글 조회", description = """
            특정 사용자의 프로필과 게시글 목록을 조회합니다.
            - 인증 필수
            - 같은 응원팀: 전체(ALL) + 팀 채널 게시글 모두 조회
            - 다른 응원팀: 전체(ALL) 채널 게시글만 조회
            - 차단한 사용자의 프로필은 조회할 수 없습니다
            - 10개씩 커서 기반 페이징""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserPostListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "차단한 사용자",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY016", "message": "차단한 사용자의 프로필을 조회할 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "USER001", "message": "사용자를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/{userId}/posts")
    public ResponseEntity<UserPostListResponse> getUserPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 사용자 ID") @PathVariable Long userId,
            @Parameter(description = "페이지 커서 (다음 페이지 조회 시)")
            @RequestParam(required = false) Long cursor) {

        UserProfilePostListDto result = userProfileAppService.getUserPosts(
                userDetails.userId(),
                userDetails.teamCode(),
                userId,
                cursor
        );
        return ResponseEntity.ok(UserPostListResponse.from(result));
    }
}
