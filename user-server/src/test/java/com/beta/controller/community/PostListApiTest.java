package com.beta.controller.community;

import com.beta.ApiTestBase;
import com.beta.controller.community.response.PostListResponse;
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
@DisplayName("게시글 목록 조회 API 통합 테스트")
@Sql(scripts = "/sql/post-list-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PostListApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // SQL에서 생성된 사용자 ID (고정)
    private static final Long TEST_USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long BLOCKED_USER_ID = 3L;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // SQL 데이터의 user_id=1 (테스트유저, DOOSAN)
        accessToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, "DOOSAN", "USER");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("GET /api/v1/community/posts")
    class GetPostList {

        @Test
        @DisplayName("채널 미지정 시 내 팀 채널 게시글만 조회한다")
        void getPostList_defaultChannel() {
            // when - channel 파라미터 없이 요청
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - DOOSAN 채널만 조회 (차단유저 제외, 삭제된 게시글 제외)
            // SQL 데이터: post 100, 101 (DOOSAN, user 1), post 105 (DOOSAN, user 3-차단)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(2);
            assertThat(response.getBody().getPosts())
                    .allMatch(p -> p.getChannel().equals("DOOSAN"));
            // 차단유저 게시글 제외 확인
            assertThat(response.getBody().getPosts())
                    .noneMatch(p -> p.getAuthor().getUserId().equals(BLOCKED_USER_ID));
        }

        @Test
        @DisplayName("channel 파라미터 지정 시 ALL 채널만 조회한다")
        void getPostList_allChannel() {
            // when - channel 파라미터 지정 (어떤 값이든 ALL로 처리)
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?channel=any",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - ALL 채널만 조회
            // SQL 데이터: post 102 (ALL, user 2)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);
            assertThat(response.getBody().getPosts().get(0).getChannel()).isEqualTo("ALL");
        }

        @Test
        @DisplayName("차단한 사용자의 게시글은 제외된다")
        void getPostList_excludesBlockedUser() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - user 3 (차단유저)의 게시글(id=105)이 없어야 함
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getPosts())
                    .noneMatch(p -> p.getAuthor().getUserId().equals(BLOCKED_USER_ID));
            assertThat(response.getBody().getPosts())
                    .noneMatch(p -> p.getPostId() == 105L);
        }

        @Test
        @DisplayName("삭제된 게시글은 제외된다")
        void getPostList_excludesDeletedPosts() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - 삭제된 게시글(id=104)이 없어야 함
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getPosts())
                    .noneMatch(p -> p.getPostId() == 104L);
        }

        @Test
        @DisplayName("커서 기반 페이징이 동작한다")
        void getPostList_cursorPaging() {
            // when - post 101 이후의 게시글 조회 (id < 101)
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?cursor=101",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 100만 조회 (DOOSAN, id < 101)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("게시글의 감정표현 카운트가 정확히 반환된다")
        void getPostList_emotionCounts() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 100의 감정 카운트 검증 (SQL: like=5, sad=1, fun=2, hype=3)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PostListResponse.PostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getEmotions().getLikeCount()).isEqualTo(5);
            assertThat(post100.getEmotions().getSadCount()).isEqualTo(1);
            assertThat(post100.getEmotions().getFunCount()).isEqualTo(2);
            assertThat(post100.getEmotions().getHypeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("게시글의 댓글 카운트가 정확히 반환된다")
        void getPostList_commentCount() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 100의 댓글 카운트 검증 (SQL: comment_count=3)
            PostListResponse.PostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getCommentCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("게시글의 이미지 정보가 정확히 반환된다 (ACTIVE만, id와 url 포함)")
        void getPostList_images() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 100의 이미지 검증 (ACTIVE 2개, DELETED 1개 제외)
            PostListResponse.PostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getImages()).hasSize(2);
            // 이미지 ID와 URL이 모두 반환되는지 확인
            assertThat(post100.getImages().get(0).getImageId()).isNotNull();
            assertThat(post100.getImages().get(0).getImageUrl()).isEqualTo("https://storage.example.com/images/post100_1.jpg");
            assertThat(post100.getImages().get(1).getImageId()).isNotNull();
            assertThat(post100.getImages().get(1).getImageUrl()).isEqualTo("https://storage.example.com/images/post100_2.jpg");
        }

        @Test
        @DisplayName("게시글의 해시태그가 정확히 반환된다")
        void getPostList_hashtags() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 100의 해시태그 검증 (야구, 두산)
            PostListResponse.PostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getHashtags()).hasSize(2);
            assertThat(post100.getHashtags()).containsExactlyInAnyOrder("야구", "두산");
        }

        @Test
        @DisplayName("작성자 정보가 정확히 반환된다")
        void getPostList_authorInfo() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 100의 작성자 정보 검증
            PostListResponse.PostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getAuthor().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(post100.getAuthor().getNickname()).isEqualTo("테스트유저");
            assertThat(post100.getAuthor().getTeamCode()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("게시글 내용이 정확히 반환된다")
        void getPostList_content() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            PostListResponse.PostSummary post100 = response.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getContent()).isEqualTo("두산 베어스 오늘 경기 최고였어요!");
        }

        @Test
        @DisplayName("인증 없이 요청하면 401 에러")
        void getPostList_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("인기순(popular) 정렬 시 감정 총합 기준으로 정렬된다")
        void getPostList_popularSort() {
            // when - sort=popular로 요청
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?sort=popular",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - 감정 총합 기준 내림차순 (post 100: 11, post 101: 3)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(2);

            // 인기순 정렬 확인: post 100(총합 11) > post 101(총합 3)
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(100L);
            assertThat(response.getBody().getPosts().get(1).getPostId()).isEqualTo(101L);

            // 인기순은 nextCursor가 null (offset 방식 사용)
            assertThat(response.getBody().getNextCursor()).isNull();
        }

        @Test
        @DisplayName("인기순 offset 페이징이 동작한다")
        void getPostList_popularOffsetPaging() {
            // when - offset=1로 요청 (두 번째 게시글부터)
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?sort=popular&offset=1",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - post 101만 조회 (post 100은 offset으로 스킵)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(101L);
            assertThat(response.getBody().isHasNext()).isFalse();
        }

        @Test
        @DisplayName("최신순(latest) 정렬 시 ID 기준으로 정렬된다")
        void getPostList_latestSort() {
            // when - sort=latest로 요청
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?sort=latest",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - ID 내림차순 (post 101 > post 100)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(2);

            // 최신순 정렬 확인: post 101(id 높음) > post 100
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(101L);
            assertThat(response.getBody().getPosts().get(1).getPostId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("최신순은 cursor를 사용하고 nextCursor를 반환한다")
        void getPostList_latestUsesCursor() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?sort=latest",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then - 데이터가 2개뿐이라 hasNext는 false, nextCursor도 null
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().isHasNext()).isFalse();
            assertThat(response.getBody().getNextCursor()).isNull();
        }
    }
}
