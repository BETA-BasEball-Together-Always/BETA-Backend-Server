package com.beta.controller.mypage;

import com.beta.ApiTestBase;
import com.beta.controller.mypage.response.MyPostListResponse;
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

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("마이페이지 API 통합 테스트")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/mypage-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MyPageApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long TEST_USER_ID = 1L;

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
    @DisplayName("GET /api/v1/mypage/posts")
    class GetMyPosts {

        @Test
        @DisplayName("내 게시글 첫 페이지 조회 성공")
        void getMyPosts_firstPage_success() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(10); // 페이지 사이즈 10
            assertThat(response.getBody().isHasNext()).isTrue();
            assertThat(response.getBody().getNextCursor()).isNotNull();
            // 최신순 정렬 확인 (id 내림차순)
            assertThat(response.getBody().getPosts().getFirst().getPostId()).isEqualTo(111L);
        }

        @Test
        @DisplayName("내 게시글 두 번째 페이지 조회")
        void getMyPosts_secondPage() {
            // given - 첫 페이지 조회
            ResponseEntity<MyPostListResponse> firstPage = restTemplate.exchange(
                    "/api/v1/mypage/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );
            Long cursor = firstPage.getBody().getNextCursor();

            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/posts?cursor=" + cursor,
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(Objects.requireNonNull(response.getBody()).getPosts()).hasSize(2); // 나머지 2개
            assertThat(response.getBody().isHasNext()).isFalse();
            assertThat(response.getBody().getNextCursor()).isNull();
        }

        @Test
        @DisplayName("게시글 상세 정보가 포함된다 (이미지, 해시태그, 감정표현)")
        void getMyPosts_containsDetails() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then - post 100 찾기 (가장 오래된 게시글)
            MyPostListResponse.MyPostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElse(null);

            // 첫 페이지(10개)에 없으면 두 번째 페이지 조회
            if (post100 == null) {
                Long cursor = response.getBody().getNextCursor();
                ResponseEntity<MyPostListResponse> secondPage = restTemplate.exchange(
                        "/api/v1/mypage/posts?cursor=" + cursor,
                        HttpMethod.GET,
                        new HttpEntity<>(createAuthHeaders()),
                        MyPostListResponse.class
                );
                post100 = secondPage.getBody().getPosts().stream()
                        .filter(p -> p.getPostId() == 100L)
                        .findFirst()
                        .orElseThrow();
            }

            assertThat(post100.getContent()).isEqualTo("내 게시글 1");
            assertThat(post100.getChannel()).isEqualTo("DOOSAN");
            assertThat(post100.getImages()).hasSize(1);
            assertThat(post100.getHashtags()).containsExactlyInAnyOrder("야구", "두산");
            assertThat(post100.getEmotions().getLikeCount()).isEqualTo(3);
            assertThat(post100.getAuthor().getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("삭제된 게시글은 조회되지 않는다")
        void getMyPosts_excludesDeleted() {
            // when - 전체 조회 (첫 페이지 + 두 번째 페이지)
            ResponseEntity<MyPostListResponse> firstPage = restTemplate.exchange(
                    "/api/v1/mypage/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            ResponseEntity<MyPostListResponse> secondPage = restTemplate.exchange(
                    "/api/v1/mypage/posts?cursor=" + firstPage.getBody().getNextCursor(),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then - 총 12개 (삭제된 게시글 id=300 제외)
            int totalPosts = firstPage.getBody().getPosts().size() + secondPage.getBody().getPosts().size();
            assertThat(totalPosts).isEqualTo(12);

            // 삭제된 게시글이 없는지 확인
            assertThat(firstPage.getBody().getPosts()).noneMatch(p -> p.getPostId() == 300L);
            assertThat(secondPage.getBody().getPosts()).noneMatch(p -> p.getPostId() == 300L);
        }

        @Test
        @DisplayName("인증 없이 요청하면 401")
        void getMyPosts_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/mypage/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/mypage/commented")
    class GetCommentedPosts {

        @Test
        @DisplayName("내가 댓글 단 게시글 조회 성공")
        void getCommentedPosts_success() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/commented",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then - user 1이 댓글 단 게시글: 200, 201, 202 (3개)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(3);
            assertThat(response.getBody().isHasNext()).isFalse();
        }

        @Test
        @DisplayName("동일 게시글에 여러 댓글을 달아도 한 번만 표시")
        void getCommentedPosts_noDuplicates() {
            // when - post 202에 댓글 2개 달았지만 게시글은 1번만
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/commented",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then
            long post202Count = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 202L)
                    .count();
            assertThat(post202Count).isEqualTo(1);
        }

        @Test
        @DisplayName("최신순 정렬 (게시글 ID 내림차순)")
        void getCommentedPosts_sortedByIdDesc() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/commented",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(202L);
            assertThat(response.getBody().getPosts().get(1).getPostId()).isEqualTo(201L);
            assertThat(response.getBody().getPosts().get(2).getPostId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("인증 없이 요청하면 401")
        void getCommentedPosts_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/mypage/commented",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/mypage/liked")
    class GetLikedPosts {

        @Test
        @DisplayName("내가 좋아요 누른 게시글 조회 성공")
        void getLikedPosts_success() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/liked",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then - user 1이 감정표현한 게시글: 100, 200, 201, 202 (4개)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(4);
            assertThat(response.getBody().isHasNext()).isFalse();
        }

        @Test
        @DisplayName("모든 감정표현 타입이 포함된다 (LIKE, SAD, FUN, HYPE)")
        void getLikedPosts_includesAllEmotionTypes() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/liked",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then - LIKE: 100, 200 / SAD: 201 / FUN: 202
            assertThat(response.getBody().getPosts().stream().map(MyPostListResponse.MyPostSummary::getPostId))
                    .containsExactlyInAnyOrder(100L, 200L, 201L, 202L);
        }

        @Test
        @DisplayName("최신순 정렬 (게시글 ID 내림차순)")
        void getLikedPosts_sortedByIdDesc() {
            // when
            ResponseEntity<MyPostListResponse> response = restTemplate.exchange(
                    "/api/v1/mypage/liked",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    MyPostListResponse.class
            );

            // then
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(202L);
            assertThat(response.getBody().getPosts().get(1).getPostId()).isEqualTo(201L);
        }

        @Test
        @DisplayName("인증 없이 요청하면 401")
        void getLikedPosts_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/mypage/liked",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
