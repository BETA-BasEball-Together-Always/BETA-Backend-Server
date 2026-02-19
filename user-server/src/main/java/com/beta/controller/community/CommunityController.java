package com.beta.controller.community;

import com.beta.community.application.CommentAppService;
import com.beta.community.application.CommunityFacadeService;
import com.beta.community.application.dto.CommentCreateDto;
import com.beta.community.application.dto.CommentLikeToggleDto;
import com.beta.community.application.dto.CommentsDto;
import com.beta.community.application.dto.EmotionToggleDto;
import com.beta.community.application.dto.PostDetailDto;
import com.beta.community.application.dto.PostDto;
import com.beta.community.application.dto.PostListDto;
import com.beta.community.application.dto.UpdatePostDto;
import com.beta.controller.community.request.*;
import com.beta.controller.community.response.*;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Community", description = "커뮤니티 관련 API")
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityFacadeService communityFacadeService;
    private final CommentAppService commentAppService;

    // ==================== 게시글 API ====================

    @Operation(summary = "게시글 리스트 조회", description = """
            channel 미지정 시 내 팀 채널, 지정 시 ALL 채널 조회.
            - sort=latest (기본값): 최신순, cursor 파라미터 사용
            - sort=popular: 인기순, offset 파라미터 사용""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponse.class))),
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
            @Parameter(description = "페이지 커서 (최신순 정렬 시), 첫 조회 시 생략")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 오프셋 (인기순 정렬 시), 기본값 0")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "채널 구분. 생략 시 내 팀 채널, 지정 시 전체 채널")
            @RequestParam(required = false) String channel,
            @Parameter(description = "정렬 방식: latest(최신순, 기본값), popular(인기순)")
            @RequestParam(defaultValue = "latest") String sort) {

        String effectiveChannel = (channel != null) ? "ALL" : userDetails.teamCode();

        PostListDto postListDto = communityFacadeService.getPostList(
                userDetails.userId(),
                cursor,
                offset,
                effectiveChannel,
                sort
        );

        return ResponseEntity.ok(PostListResponse.from(postListDto));
    }

    @Operation(summary = "게시글 상세 조회", description = """
            게시글 상세 정보와 댓글 첫 페이지(20개)를 조회합니다.
            - 댓글은 최신순으로 정렬됩니다.
            - 추가 댓글은 GET /posts/{postId}/comments API로 조회하세요.
            - 삭제된 댓글은 "삭제된 댓글입니다"로 표시되며, 답글이 없으면 표시하지 않습니다.
            - 차단한 사용자의 댓글은 표시되지 않습니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {

        PostDetailDto postDetailDto = communityFacadeService.getPostDetail(
                userDetails.userId(),
                postId
        );

        return ResponseEntity.ok(PostDetailResponse.from(postDetailDto));
    }

    @Operation(summary = "댓글 추가 조회", description = """
            게시글의 댓글을 커서 기반으로 추가 조회합니다.
            - 부모 댓글 기준 20개씩 조회합니다.
            - 각 부모 댓글의 대댓글은 모두 포함됩니다.
            - hasNext=true면 nextCursor로 다음 페이지를 조회하세요.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentsResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentsResponse> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "댓글 커서 (다음 페이지 조회 시)")
            @RequestParam(required = false) Long cursor) {

        CommentsDto commentsDto = communityFacadeService.getComments(
                userDetails.userId(),
                postId,
                cursor
        );

        return ResponseEntity.ok(CommentsResponse.from(commentsDto));
    }

    @Operation(summary = "게시글 작성", description = """
            새 게시글을 작성합니다.
            - channel=TEAM: 내 팀 채널에 게시
            - channel=ALL: 전체 채널에 게시
            - 이미지: 최대 5개, 각 10MB 이하 (JPG/PNG/GIF/WEBP)
            - 해시태그: 최대 5개, 각 20자 이하
            - 30초 내 동일 내용 게시글 중복 방지""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreatePostResponse.class))),
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

    @Operation(summary = "게시글 수정", description = """
            게시글을 수정합니다.
            - 기존 이미지 삭제: deletedImageIds에 이미지 ID 목록 전달 (목록/상세 조회 응답의 imageId 사용)
            - 새 이미지 추가: newImages에 파일 첨부
            - 최종 이미지 개수가 5개를 초과하면 실패합니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreatePostResponse.class))),
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
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
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

    @Operation(summary = "게시글 삭제 (소프트)", description = """
            게시글을 삭제합니다.
            - 소프트 삭제 방식: 실제로 DB에서 삭제되지 않고 상태만 DELETED로 변경됩니다.
            - 관련 이미지, 해시태그도 함께 비활성화됩니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))),
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
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {

        communityFacadeService.deletePost(userDetails.userId(), postId);
        return ResponseEntity.ok(MessageResponse.of("게시글이 삭제되었습니다."));
    }

    // ==================== 감정표현 API ====================

    @Operation(summary = "감정표현 토글", description = """
            게시글에 감정표현을 토글합니다.
            - 기존에 없던 감정: 추가
            - 같은 감정을 다시 요청: 제거
            - 다른 감정으로 변경: 이전 감정 제거 후 새 감정 추가
            - 응답의 toggled=true: 감정이 추가됨, false: 감정이 제거됨""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmotionResponse.class))),
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
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Valid @RequestBody EmotionRequest request) {

        EmotionToggleDto result = communityFacadeService.toggleEmotion(
                userDetails.userId(),
                postId,
                request.getEmotionType()
        );

        return ResponseEntity.ok(EmotionResponse.builder()
                .postId(result.getPostId())
                .emotionType(result.getEmotionType())
                .toggled(result.isToggled())
                .emotions(PostListResponse.EmotionCount.builder()
                        .likeCount(result.getLikeCount())
                        .sadCount(result.getSadCount())
                        .funCount(result.getFunCount())
                        .hypeCount(result.getHypeCount())
                        .build())
                .build());
    }

    // ==================== 댓글 API ====================

    @Operation(summary = "댓글/답글 작성", description = """
            댓글 또는 답글을 작성합니다.
            - parentId=null: 최상위 댓글 (depth=0)
            - parentId=댓글ID: 해당 댓글에 대한 답글 (depth=1)
            - 답글의 답글(depth=2)은 지원하지 않습니다.
            - 30초 내 동일 내용 댓글 중복 방지""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 답글 깊이 초과",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "답글 깊이 초과", value = """
                                            {"code": "COMMUNITY011", "message": "답글은 1단계까지만 가능합니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "입력값 검증 실패", value = """
                                            {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "게시글 없음", value = """
                                            {"code": "COMMUNITY004", "message": "게시글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "부모 댓글 없음", value = """
                                            {"code": "COMMUNITY008", "message": "댓글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "409", description = "중복 댓글",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY012", "message": "동일한 내용의 댓글이 최근에 작성되었습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentCreateResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {

        CommentCreateDto result = commentAppService.createComment(
                userDetails.userId(),
                postId,
                request.getContent(),
                request.getParentId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(CommentCreateResponse.builder()
                .commentId(result.getCommentId())
                .postId(result.getPostId())
                .userId(result.getUserId())
                .content(result.getContent())
                .parentId(result.getParentId())
                .depth(result.getDepth())
                .createdAt(result.getCreatedAt())
                .build());
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글의 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION001", "message": "입력값 검증에 실패했습니다", "timestamp": "2025-01-01T00:00:00"}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
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
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        commentAppService.updateComment(
                userDetails.userId(),
                commentId,
                request.getContent()
        );

        return ResponseEntity.ok(MessageResponse.of("댓글이 수정되었습니다."));
    }

    @Operation(summary = "댓글 삭제 (소프트)", description = """
            댓글을 삭제합니다.
            - 소프트 삭제 방식
            - 답글이 있는 댓글은 "삭제된 댓글입니다"로 표시됩니다.
            - 답글이 없는 댓글은 목록에서 완전히 숨겨집니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
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
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {

        commentAppService.deleteComment(userDetails.userId(), commentId);

        return ResponseEntity.ok(MessageResponse.of("댓글이 삭제되었습니다."));
    }

    @Operation(summary = "댓글 좋아요 토글", description = """
            댓글에 좋아요를 토글합니다.
            - 기존에 좋아요가 없으면 추가
            - 이미 좋아요했으면 제거
            - 응답의 liked=true: 좋아요 추가됨, false: 좋아요 제거됨""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentLikeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "토큰 만료", value = """
                                            {"code": "JWT001", "message": "토큰이 만료되었습니다", "timestamp": "2025-01-01T00:00:00"}"""),
                                    @ExampleObject(name = "토큰 무효", value = """
                                            {"code": "JWT002", "message": "유효하지 않은 토큰입니다", "timestamp": "2025-01-01T00:00:00"}""")
                            })),
            @ApiResponse(responseCode = "404", description = "댓글 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY008", "message": "댓글을 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> toggleCommentLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {

        CommentLikeToggleDto result = commentAppService.toggleCommentLike(
                userDetails.userId(),
                commentId
        );

        return ResponseEntity.ok(CommentLikeResponse.builder()
                .commentId(result.getCommentId())
                .liked(result.isLiked())
                .likeCount(result.getLikeCount())
                .build());
    }

    // ==================== 사용자 차단 API ====================

    @Operation(summary = "사용자 차단", description = """
            특정 사용자를 차단합니다.
            - 차단된 사용자의 게시글은 목록에서 필터링됩니다.
            - 자기 자신은 차단할 수 없습니다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BlockResponse.class))),
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
            @Parameter(description = "차단할 사용자 ID") @PathVariable Long userId) {

        communityFacadeService.blockUser(userDetails.userId(), userId);
        return ResponseEntity.ok(BlockResponse.builder()
                .userId(userDetails.userId())
                .blockedUserId(userId)
                .blocked(true)
                .build());
    }

    @Operation(summary = "사용자 차단 해제", description = "차단했던 사용자의 차단을 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 해제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BlockResponse.class))),
            @ApiResponse(responseCode = "404", description = "차단 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code": "COMMUNITY015", "message": "차단 정보를 찾을 수 없습니다", "timestamp": "2025-01-01T00:00:00"}""")))
    })
    @DeleteMapping("/users/{userId}/block")
    public ResponseEntity<BlockResponse> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "차단 해제할 사용자 ID") @PathVariable Long userId) {

        communityFacadeService.unblockUser(userDetails.userId(), userId);
        return ResponseEntity.ok(BlockResponse.builder()
                .userId(userDetails.userId())
                .blockedUserId(userId)
                .blocked(false)
                .build());
    }

}
