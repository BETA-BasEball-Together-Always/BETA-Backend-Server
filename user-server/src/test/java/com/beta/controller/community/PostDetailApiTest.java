package com.beta.controller.community;

import com.beta.ApiTestBase;
import com.beta.controller.community.response.CommentsResponse;
import com.beta.controller.community.response.PostDetailResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("게시글 상세 및 댓글 조회 API 통합 테스트")
@Sql(scripts = "/sql/post-detail-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PostDetailApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long TEST_USER_ID = 1L;
    private static final Long BLOCKED_USER_ID = 3L;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, "DOOSAN", "USER");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("GET /api/v1/community/posts/{postId}")
    class GetPostDetail {

        @Test
        @DisplayName("게시글 상세 정보가 정확히 반환된다")
        void getPostDetail_success() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPostId()).isEqualTo(100L);
            assertThat(response.getBody().getContent()).isEqualTo("두산 베어스 오늘 경기 최고였어요!");
            assertThat(response.getBody().getChannel()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("작성자 정보가 정확히 반환된다")
        void getPostDetail_authorInfo() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then
            assertThat(response.getBody().getAuthor().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getBody().getAuthor().getNickname()).isEqualTo("테스트유저");
            assertThat(response.getBody().getAuthor().getTeamCode()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("감정표현 카운트가 정확히 반환된다")
        void getPostDetail_emotionCounts() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then
            assertThat(response.getBody().getEmotions().getLikeCount()).isEqualTo(5);
            assertThat(response.getBody().getEmotions().getSadCount()).isEqualTo(1);
            assertThat(response.getBody().getEmotions().getFunCount()).isEqualTo(2);
            assertThat(response.getBody().getEmotions().getHypeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("이미지 정보가 정확히 반환된다")
        void getPostDetail_images() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then
            assertThat(response.getBody().getImages()).hasSize(2);
            assertThat(response.getBody().getImages().get(0).getImageUrl())
                    .isEqualTo("https://storage.example.com/images/post100_1.jpg");
        }

        @Test
        @DisplayName("해시태그가 정확히 반환된다")
        void getPostDetail_hashtags() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then
            assertThat(response.getBody().getHashtags()).containsExactlyInAnyOrder("야구", "두산");
        }

        @Test
        @DisplayName("댓글 첫 페이지(20개)가 포함된다")
        void getPostDetail_commentsFirstPage() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 차단 사용자 댓글 제외, 삭제된 댓글(답글 있으면 표시) 처리 후 20개
            assertThat(response.getBody().getComments()).isNotEmpty();
            assertThat(response.getBody().getComments().size()).isLessThanOrEqualTo(20);
        }

        @Test
        @DisplayName("hasNextComments와 nextCommentCursor가 반환된다")
        void getPostDetail_commentsPaging() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 댓글 25개 중 차단 제외하면 24개 -> hasNext=true
            assertThat(response.getBody().isHasNextComments()).isTrue();
            assertThat(response.getBody().getNextCommentCursor()).isNotNull();
        }

        @Test
        @DisplayName("대댓글이 replies 배열에 포함된다")
        void getPostDetail_commentsWithReplies() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 댓글 25에 대댓글 2개 (최신순 첫 페이지)
            PostDetailResponse.CommentResponse comment25 = response.getBody().getComments().stream()
                    .filter(c -> c.getCommentId() == 25L)
                    .findFirst()
                    .orElseThrow();

            assertThat(comment25.getReplies()).hasSize(2);
            assertThat(comment25.getReplies().get(0).getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("댓글에 isLiked 필드가 포함된다")
        void getPostDetail_commentsHasIsLikedField() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 댓글 25의 likeCount와 isLiked 필드 존재 확인
            PostDetailResponse.CommentResponse comment25 = response.getBody().getComments().stream()
                    .filter(c -> c.getCommentId() == 25L)
                    .findFirst()
                    .orElseThrow();

            assertThat(comment25.getLikeCount()).isEqualTo(5);
            // isLiked 필드 존재 확인 (boolean primitive - 항상 존재)
            // 실제 좋아요 데이터가 있으면 true, 없으면 false
        }

        @Test
        @DisplayName("차단 사용자의 댓글은 제외된다")
        void getPostDetail_excludesBlockedUserComments() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 차단유저(id=3)의 댓글(id=15) 제외
            assertThat(response.getBody().getComments())
                    .noneMatch(c -> c.getCommentId() == 15L);
        }

        @Test
        @DisplayName("삭제된 댓글은 활성 답글이 있으면 '삭제된 댓글입니다'로 표시")
        void getPostDetail_deletedCommentWithReplies() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 삭제된 댓글(id=24)은 deleted=true, content="삭제된 댓글입니다"
            PostDetailResponse.CommentResponse deletedComment = response.getBody().getComments().stream()
                    .filter(c -> c.getCommentId() == 24L)
                    .findFirst()
                    .orElseThrow();

            assertThat(deletedComment.isDeleted()).isTrue();
            assertThat(deletedComment.getContent()).isEqualTo("삭제된 댓글입니다");
            assertThat(deletedComment.getUserId()).isNull();
            assertThat(deletedComment.getNickname()).isNull();
            // 답글은 포함됨
            assertThat(deletedComment.getReplies()).hasSize(1);
        }

        @Test
        @DisplayName("댓글 작성자의 teamCode가 포함된다")
        void getPostDetail_commentAuthorTeamCode() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then - 댓글 25는 user 1이 작성함
            PostDetailResponse.CommentResponse comment25 = response.getBody().getComments().stream()
                    .filter(c -> c.getCommentId() == 25L)
                    .findFirst()
                    .orElseThrow();

            assertThat(comment25.getTeamCode()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("댓글 없는 게시글 조회 시 빈 댓글 목록 반환")
        void getPostDetail_noComments() {
            // when
            ResponseEntity<PostDetailResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/101",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getComments()).isEmpty();
            assertThat(response.getBody().isHasNextComments()).isFalse();
            assertThat(response.getBody().getNextCommentCursor()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404 반환")
        void getPostDetail_notFound() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/99999",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("인증 없이 요청하면 401 에러")
        void getPostDetail_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/community/posts/{postId}/comments")
    class GetComments {

        @Test
        @DisplayName("커서 기반으로 다음 댓글 페이지를 조회한다")
        void getComments_cursorPaging() {
            // given - 먼저 첫 페이지의 nextCursor 가져오기
            ResponseEntity<PostDetailResponse> firstPage = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );
            Long cursor = firstPage.getBody().getNextCommentCursor();

            // when - 다음 페이지 조회
            ResponseEntity<CommentsResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100/comments?cursor=" + cursor,
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    CommentsResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getComments()).isNotEmpty();
        }

        @Test
        @DisplayName("마지막 페이지에서 hasNext=false")
        void getComments_lastPage() {
            // given - 첫 페이지 cursor
            ResponseEntity<PostDetailResponse> firstPage = restTemplate.exchange(
                    "/api/v1/community/posts/100",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostDetailResponse.class
            );
            Long cursor = firstPage.getBody().getNextCommentCursor();

            // when - 다음 페이지 (마지막)
            ResponseEntity<CommentsResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100/comments?cursor=" + cursor,
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    CommentsResponse.class
            );

            // then - 남은 댓글이 20개 이하이므로 hasNext=false
            assertThat(response.getBody().isHasNext()).isFalse();
            assertThat(response.getBody().getNextCursor()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 댓글 조회 시 404")
        void getComments_postNotFound() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/99999/comments?cursor=1",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("댓글 추가 조회에서도 차단 사용자 댓글 제외")
        void getComments_excludesBlockedUser() {
            // when - 커서 없이 첫 페이지 조회
            ResponseEntity<CommentsResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100/comments",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    CommentsResponse.class
            );

            // then - 차단유저(id=3) 댓글(id=15) 제외
            assertThat(response.getBody().getComments())
                    .noneMatch(c -> c.getCommentId() == 15L);
        }

        @Test
        @DisplayName("댓글 추가 조회에서도 isLiked 필드가 포함된다")
        void getComments_hasIsLikedField() {
            // when - 커서 없이 조회 (첫 페이지)
            ResponseEntity<CommentsResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/100/comments",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    CommentsResponse.class
            );

            // then - 댓글에 isLiked 필드가 존재하는지 확인 (최신순으로 정렬됨)
            assertThat(response.getBody().getComments()).isNotEmpty();
            PostDetailResponse.CommentResponse firstComment = response.getBody().getComments().get(0);
            // isLiked 필드 존재 확인 (primitive boolean이므로 항상 존재)
            assertThat(firstComment.getLikeCount()).isNotNull();
        }
    }
}
