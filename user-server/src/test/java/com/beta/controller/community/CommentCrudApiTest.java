package com.beta.controller.community;

import com.beta.ApiTestBase;
import com.beta.controller.community.response.CommentCreateResponse;
import com.beta.controller.community.response.CommentLikeResponse;
import com.beta.controller.community.response.MessageResponse;
import com.beta.core.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("댓글 CRUD 및 좋아요 API 통합 테스트")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/comment-crud-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CommentCrudApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long TEST_USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long POST_ID = 100L;

    private String accessToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, "DOOSAN", "USER");
        otherUserToken = jwtTokenProvider.generateAccessToken(OTHER_USER_ID, "DOOSAN", "USER");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders createOtherUserHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(otherUserToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("POST /api/v1/community/posts/{postId}/comments")
    class CreateComment {

        @Test
        @DisplayName("최상위 댓글 작성 성공")
        void createComment_topLevel_success() {
            // given
            Map<String, Object> request = Map.of("content", "새로운 댓글입니다");

            // when
            ResponseEntity<CommentCreateResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_ID + "/comments",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    CommentCreateResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPostId()).isEqualTo(POST_ID);
            assertThat(response.getBody().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getBody().getContent()).isEqualTo("새로운 댓글입니다");
            assertThat(response.getBody().getParentId()).isNull();
            assertThat(response.getBody().getDepth()).isEqualTo(0);
        }

        @Test
        @DisplayName("답글 작성 성공 (depth=1)")
        void createComment_reply_success() {
            // given
            Long parentCommentId = 2L;
            Map<String, Object> request = Map.of(
                    "content", "답글입니다",
                    "parentId", parentCommentId
            );

            // when
            ResponseEntity<CommentCreateResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_ID + "/comments",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    CommentCreateResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getParentId()).isEqualTo(parentCommentId);
            assertThat(response.getBody().getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 내용 시 400 에러")
        void createComment_emptyContent_badRequest() {
            // given
            Map<String, Object> request = Map.of("content", "");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_ID + "/comments",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 작성 시 404")
        void createComment_postNotFound_notFound() {
            // given
            Map<String, Object> request = Map.of("content", "댓글");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/99999/comments",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("인증 없이 댓글 작성 시 401")
        void createComment_unauthorized() {
            // given
            Map<String, Object> request = Map.of("content", "댓글");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_ID + "/comments",
                    HttpMethod.POST,
                    new HttpEntity<>(request, new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/community/comments/{commentId}")
    class UpdateComment {

        @Test
        @DisplayName("본인 댓글 수정 성공")
        void updateComment_ownComment_success() {
            // given
            Long myCommentId = 1L;
            Map<String, Object> request = Map.of("content", "수정된 댓글입니다");

            // when
            ResponseEntity<MessageResponse> response = restTemplate.exchange(
                    "/api/v1/community/comments/" + myCommentId,
                    HttpMethod.PUT,
                    new HttpEntity<>(request, createAuthHeaders()),
                    MessageResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("수정");
        }

        @Test
        @DisplayName("타인 댓글 수정 시 403")
        void updateComment_otherComment_forbidden() {
            // given
            Long otherUserCommentId = 2L;
            Map<String, Object> request = Map.of("content", "수정 시도");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/comments/" + otherUserCommentId,
                    HttpMethod.PUT,
                    new HttpEntity<>(request, createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시 404")
        void updateComment_notFound() {
            // given
            Map<String, Object> request = Map.of("content", "수정");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/comments/99999",
                    HttpMethod.PUT,
                    new HttpEntity<>(request, createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/community/comments/{commentId}")
    class DeleteComment {

        @Test
        @DisplayName("본인 댓글 삭제 성공")
        void deleteComment_ownComment_success() {
            // given
            Long myCommentId = 1L;

            // when
            ResponseEntity<MessageResponse> response = restTemplate.exchange(
                    "/api/v1/community/comments/" + myCommentId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(createAuthHeaders()),
                    MessageResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("삭제");
        }

        @Test
        @DisplayName("타인 댓글 삭제 시 403")
        void deleteComment_otherComment_forbidden() {
            // given
            Long otherUserCommentId = 2L;

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/comments/" + otherUserCommentId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 404")
        void deleteComment_notFound() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/comments/99999",
                    HttpMethod.DELETE,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/community/comments/{commentId}/like")
    class ToggleCommentLike {

        @Test
        @DisplayName("댓글 좋아요 추가")
        void toggleCommentLike_add() {
            // given - 댓글 2에는 user 1의 좋아요가 없음
            Long commentId = 2L;

            // when
            ResponseEntity<CommentLikeResponse> response = restTemplate.exchange(
                    "/api/v1/community/comments/" + commentId + "/like",
                    HttpMethod.POST,
                    new HttpEntity<>(createAuthHeaders()),
                    CommentLikeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCommentId()).isEqualTo(commentId);
            assertThat(response.getBody().isLiked()).isTrue();
            assertThat(response.getBody().getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("댓글 좋아요 취소")
        void toggleCommentLike_remove() {
            // given - 댓글 1에 user 1이 이미 좋아요함
            Long commentId = 1L;

            // when
            ResponseEntity<CommentLikeResponse> response = restTemplate.exchange(
                    "/api/v1/community/comments/" + commentId + "/like",
                    HttpMethod.POST,
                    new HttpEntity<>(createAuthHeaders()),
                    CommentLikeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isLiked()).isFalse();
            assertThat(response.getBody().getLikeCount()).isEqualTo(1); // 2 -> 1
        }

        @Test
        @DisplayName("존재하지 않는 댓글 좋아요 시 404")
        void toggleCommentLike_notFound() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/comments/99999/like",
                    HttpMethod.POST,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("인증 없이 좋아요 시 401")
        void toggleCommentLike_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/comments/1/like",
                    HttpMethod.POST,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
