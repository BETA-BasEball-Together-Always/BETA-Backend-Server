package com.beta.controller.mypage;

import com.beta.account.application.MyPageAppService;
import com.beta.account.application.dto.MyPostListDto;
import com.beta.controller.mypage.response.MyPostListResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyPage", description = "마이페이지 API")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageAppService myPageAppService;

    @Operation(summary = "내 게시글 조회", description = """
            내가 작성한 게시글 목록을 조회합니다.
            - 10개씩 커서 기반 페이징
            - 최신순(게시글 ID 내림차순) 정렬
            - hasNext=true면 nextCursor로 다음 페이지 조회""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MyPostListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/posts")
    public ResponseEntity<MyPostListResponse> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 커서 (다음 페이지 조회 시)")
            @RequestParam(required = false) Long cursor) {

        MyPostListDto result = myPageAppService.getMyPosts(userDetails.userId(), cursor);
        return ResponseEntity.ok(MyPostListResponse.from(result));
    }

    @Operation(summary = "내가 댓글 단 게시글 조회", description = """
            내가 댓글을 작성한 게시글 목록을 조회합니다.
            - 10개씩 커서 기반 페이징
            - 최신순(게시글 ID 내림차순) 정렬
            - 동일 게시글에 여러 댓글을 달았어도 게시글은 한 번만 표시됩니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MyPostListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/commented")
    public ResponseEntity<MyPostListResponse> getCommentedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 커서 (다음 페이지 조회 시)")
            @RequestParam(required = false) Long cursor) {

        MyPostListDto result = myPageAppService.getCommentedPosts(userDetails.userId(), cursor);
        return ResponseEntity.ok(MyPostListResponse.from(result));
    }

    @Operation(summary = "내가 좋아요 누른 게시글 조회", description = """
            내가 감정표현(좋아요/슬퍼요/웃겨요/열광해요)을 누른 게시글 목록을 조회합니다.
            - 10개씩 커서 기반 페이징
            - 최신순(게시글 ID 내림차순) 정렬""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MyPostListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            }))
    })
    @GetMapping("/liked")
    public ResponseEntity<MyPostListResponse> getLikedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 커서 (다음 페이지 조회 시)")
            @RequestParam(required = false) Long cursor) {

        MyPostListDto result = myPageAppService.getLikedPosts(userDetails.userId(), cursor);
        return ResponseEntity.ok(MyPostListResponse.from(result));
    }
}
