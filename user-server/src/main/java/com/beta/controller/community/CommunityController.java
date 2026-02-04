package com.beta.controller.community;

import com.beta.community.application.CommunityFacadeService;
import com.beta.community.application.dto.PostDto;
import com.beta.community.application.dto.UpdatePostDto;
import com.beta.controller.community.request.*;
import com.beta.controller.community.response.*;
import com.beta.core.response.ErrorResponse;
import com.beta.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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

    @Operation(summary = "게시글 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/posts")
    public ResponseEntity<PostListResponse> getPostList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "ALL") String channel) {

        // TODO: 실제 구현 예정 (Step 5)
        PostListResponse mock = PostListResponse.builder()
                .posts(List.of())
                .hasNext(false)
                .nextCursor(null)
                .build();
        return ResponseEntity.ok(mock);
    }

    @Operation(summary = "게시글 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음 (COMMUNITY004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (COMMUNITY002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (COMMUNITY001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복 게시글 (COMMUNITY005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 (COMMUNITY003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 이미지 검증 실패 (COMMUNITY002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (COMMUNITY006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음 (COMMUNITY004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 실패 (COMMUNITY003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "403", description = "권한 없음 (COMMUNITY006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음 (COMMUNITY004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음 (COMMUNITY004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 / 답글 깊이 초과 (COMMUNITY011)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음 (COMMUNITY004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복 댓글 (COMMUNITY012)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (COMMUNITY009)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 없음 (COMMUNITY008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "403", description = "권한 없음 (COMMUNITY009)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 없음 (COMMUNITY008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "404", description = "댓글 없음 (COMMUNITY008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "400", description = "자기 자신 차단 불가 (COMMUNITY013)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 차단됨 (COMMUNITY014)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<BlockResponse> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {

        // TODO: 실제 구현 예정 (Step 4)
        BlockResponse mock = BlockResponse.builder()
                .userId(userDetails.userId())
                .blockedUserId(userId)
                .blocked(true)
                .build();
        return ResponseEntity.ok(mock);
    }

    @Operation(summary = "사용자 차단 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 해제 성공"),
            @ApiResponse(responseCode = "404", description = "차단 정보 없음 (COMMUNITY015)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/users/{userId}/block")
    public ResponseEntity<BlockResponse> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {

        // TODO: 실제 구현 예정 (Step 4)
        BlockResponse mock = BlockResponse.builder()
                .userId(userDetails.userId())
                .blockedUserId(userId)
                .blocked(false)
                .build();
        return ResponseEntity.ok(mock);
    }
}
