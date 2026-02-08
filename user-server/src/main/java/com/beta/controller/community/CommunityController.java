package com.beta.controller.community;

import com.beta.community.application.CommunityFacadeService;
import com.beta.community.application.dto.PostDto;
import com.beta.community.application.dto.PostListDto;
import com.beta.community.application.dto.UpdatePostDto;
import com.beta.controller.community.request.*;
import com.beta.controller.community.response.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Community", description = "커뮤니티 관련 API")
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityFacadeService communityFacadeService;

    // ==================== 게시글 API ====================

    @Operation(summary = "게시글 리스트 조회", description = "channel 미지정 시 내 팀 채널, 지정 시 ALL 채널 조회. sort: latest(최신순, 기본값), popular(인기순)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
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
    public ResponseEntity<PostListResponse> getPostList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "latest") String sort) {

        String effectiveChannel = (channel != null) ? "ALL" : userDetails.teamCode();

        PostListDto postListDto = communityFacadeService.getPostList(
                userDetails.userId(),
                cursor,
                effectiveChannel,
                sort
        );

        return ResponseEntity.ok(PostListResponse.from(postListDto));
    }

    @Operation(summary = "게시글 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam(defaultValue = "latest") String commentSort) {

        // TODO: 실제 구현 예정 (Step 6)
        PostDetailResponse mock = PostDetailResponse.builder()
                .postId(postId)
                .content("mock content")
                .channel("ALL")
                .imageUrls(List.of())
                .hashtags(List.of())
                .author(PostListResponse.AuthorInfo.builder()
                        .userId(1L)
                        .nickname("mock")
                        .teamCode("DOOSAN")
                        .build())
                .emotions(PostListResponse.EmotionCount.builder()
                        .likeCount(0).sadCount(0).funCount(0).hypeCount(0)
                        .build())
                .commentCount(0)
                .createdAt(LocalDateTime.now())
                .comments(List.of())
                .build();
        return ResponseEntity.ok(mock);
    }

    @Operation(summary = "게시글 작성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 이미지 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "이미지 검증 실패", value = """
                                            {"code": "COMMUNITY002", "message": "유효하지 않은 이미지입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "채널 접근 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY001", "message": "채널 접근 권한이 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "409", description = "중복 게시글",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY005", "message": "동일한 내용의 게시글이 최근에 작성되었습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY003", "message": "이미지 업로드에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatePostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute CreatePostRequest request) {

        PostDto postDto = communityFacadeService.createPost(
                userDetails.userId(),
                userDetails.teamCode(),
                request.toDto()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(CreatePostResponse.from(postDto));
    }

    @Operation(summary = "게시글 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 이미지 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "이미지 검증 실패", value = """
                                            {"code": "COMMUNITY002", "message": "유효하지 않은 이미지입니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "403", description = "게시글 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY006", "message": "게시글에 대한 권한이 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY003", "message": "이미지 업로드에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PutMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatePostResponse> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @ModelAttribute UpdatePostRequest request) {

        PostDto postDto = communityFacadeService.updatePost(
                userDetails.userId(),
                postId,
                UpdatePostDto.builder()
                        .content(request.getContent())
                        .hashtags(request.getHashtags())
                        .deletedImageIds(request.getDeletedImageIds())
                        .newImages(request.getNewImages())
                        .build()
        );
        return ResponseEntity.ok(CreatePostResponse.from(postDto));
    }

    @Operation(summary = "게시글 삭제 (소프트)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "게시글 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY006", "message": "게시글에 대한 권한이 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<MessageResponse> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        communityFacadeService.deletePost(userDetails.userId(), postId);
        return ResponseEntity.ok(MessageResponse.of("게시글이 삭제되었습니다."));
    }

    // ==================== 감정표현 API ====================

    @Operation(summary = "감정표현 토글")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/posts/{postId}/emotions")
    public ResponseEntity<EmotionResponse> toggleEmotion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody EmotionRequest request) {

        // TODO: 실제 구현 예정 (Step 7)
        EmotionResponse mock = EmotionResponse.builder()
                .postId(postId)
                .emotionType(request.getEmotionType())
                .toggled(true)
                .emotions(PostListResponse.EmotionCount.builder()
                        .likeCount(0).sadCount(0).funCount(0).hypeCount(0)
                        .build())
                .build();
        return ResponseEntity.ok(mock);
    }

    // ==================== 댓글 API ====================

    @Operation(summary = "댓글/답글 작성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 답글 깊이 초과",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "답글 깊이 초과", value = """
                                            {"code": "COMMUNITY011", "message": "답글은 1단계까지만 가능합니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "409", description = "중복 댓글",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY012", "message": "동일한 내용의 댓글이 최근에 작성되었습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentCreateResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {

        // TODO: 실제 구현 예정 (Step 8)
        CommentCreateResponse mock = CommentCreateResponse.builder()
                .commentId(1L)
                .postId(postId)
                .userId(userDetails.userId())
                .content(request.getContent())
                .parentId(request.getParentId())
                .depth(request.getParentId() != null ? 1 : 0)
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(mock);
    }

    @Operation(summary = "댓글 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "403", description = "댓글 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY009", "message": "댓글에 대한 권한이 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY008", "message": "댓글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<MessageResponse> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        // TODO: 실제 구현 예정 (Step 8)
        return ResponseEntity.ok(MessageResponse.of("댓글이 수정되었습니다."));
    }

    @Operation(summary = "댓글 삭제 (소프트)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "댓글 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY009", "message": "댓글에 대한 권한이 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "404", description = "댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY008", "message": "댓글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {

        // TODO: 실제 구현 예정 (Step 8)
        return ResponseEntity.ok(MessageResponse.of("댓글이 삭제되었습니다."));
    }

    @Operation(summary = "댓글 좋아요 토글")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY008", "message": "댓글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> toggleCommentLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {

        // TODO: 실제 구현 예정 (Step 8)
        CommentLikeResponse mock = CommentLikeResponse.builder()
                .commentId(commentId)
                .liked(true)
                .likeCount(1)
                .build();
        return ResponseEntity.ok(mock);
    }

    // ==================== 사용자 차단 API ====================

    @Operation(summary = "사용자 차단")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신 차단 불가",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY013", "message": "자기 자신을 차단할 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "409", description = "이미 차단됨",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY014", "message": "이미 차단된 사용자입니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<BlockResponse> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {

        communityFacadeService.blockUser(userDetails.userId(), userId);
        return ResponseEntity.ok(BlockResponse.builder()
                .userId(userDetails.userId())
                .blockedUserId(userId)
                .blocked(true)
                .build());
    }

    @Operation(summary = "사용자 차단 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 해제 성공"),
            @ApiResponse(responseCode = "404", description = "차단 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY015", "message": "차단 정보를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @DeleteMapping("/users/{userId}/block")
    public ResponseEntity<BlockResponse> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {

        communityFacadeService.unblockUser(userDetails.userId(), userId);
        return ResponseEntity.ok(BlockResponse.builder()
                .userId(userDetails.userId())
                .blockedUserId(userId)
                .blocked(false)
                .build());
    }

}
