package com.beta.controller.user;

import com.beta.ApiTestBase;
import com.beta.controller.user.response.UserPostListResponse;
import com.beta.core.response.ErrorResponse;
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
@DisplayName("사용자 프로필 API 통합 테스트")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/user-profile-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserProfileApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long VIEWER_USER_ID = 1L;
    private static final Long SAME_TEAM_USER_ID = 2L;
    private static final Long OTHER_TEAM_USER_ID = 3L;
    private static final Long BLOCKED_USER_ID = 4L;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenProvider.generateAccessToken(VIEWER_USER_ID, "DOOSAN", "USER");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/posts")
    class GetUserPosts {

        @Test
        @DisplayName("같은 팀 사용자 프로필 조회 - ALL + 팀 채널 게시글 모두 반환")
        void getUserPosts_sameTeam_returnsAllAndTeamChannelPosts() {
            // when
            ResponseEntity<UserPostListResponse> response = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            // 프로필 정보 확인
            assertThat(response.getBody().getUser().getUserId()).isEqualTo(SAME_TEAM_USER_ID);
            assertThat(response.getBody().getUser().getNickname()).isEqualTo("같은팀유저");
            assertThat(response.getBody().getUser().getTeamCode()).isEqualTo("DOOSAN");

            // 페이징 (10개)
            assertThat(response.getBody().getPosts()).hasSize(10);
            assertThat(response.getBody().isHasNext()).isTrue();

            // ALL, DOOSAN 채널만 포함
            assertThat(response.getBody().getPosts())
                    .allMatch(post -> post.getChannel().equals("ALL") || post.getChannel().equals("DOOSAN"));
        }

        @Test
        @DisplayName("다른 팀 사용자 프로필 조회 - ALL 채널 게시글만 반환")
        void getUserPosts_differentTeam_returnsOnlyAllChannelPosts() {
            // when
            ResponseEntity<UserPostListResponse> response = restTemplate.exchange(
                    "/api/v1/users/" + OTHER_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            // 프로필 정보 확인
            assertThat(response.getBody().getUser().getUserId()).isEqualTo(OTHER_TEAM_USER_ID);
            assertThat(response.getBody().getUser().getNickname()).isEqualTo("다른팀유저");
            assertThat(response.getBody().getUser().getTeamCode()).isEqualTo("LG");

            // ALL 채널만 반환 (2개)
            assertThat(response.getBody().getPosts()).hasSize(2);
            assertThat(response.getBody().isHasNext()).isFalse();
            assertThat(response.getBody().getPosts())
                    .allMatch(post -> post.getChannel().equals("ALL"));
        }

        @Test
        @DisplayName("차단한 사용자 프로필 조회 - 403 에러")
        void getUserPosts_blockedUser_returns403() {
            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    "/api/v1/users/" + BLOCKED_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("COMMUNITY016");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - 404 에러")
        void getUserPosts_userNotFound_returns404() {
            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    "/api/v1/users/9999/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("USER001");
        }

        @Test
        @DisplayName("인증 없이 요청 - 401 에러")
        void getUserPosts_unauthorized_returns401() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("커서 기반 페이징 동작 확인")
        void getUserPosts_cursorPaging_works() {
            // given - 첫 페이지 조회
            ResponseEntity<UserPostListResponse> firstPage = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );
            Long cursor = firstPage.getBody().getNextCursor();

            // when - 두 번째 페이지 조회
            ResponseEntity<UserPostListResponse> secondPage = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts?cursor=" + cursor,
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            // then
            assertThat(secondPage.getStatusCode()).isEqualTo(HttpStatus.OK);
            // user 2의 게시글: ALL 3개 + DOOSAN 2개 + ALL 페이징 12개 = 17개
            // 첫 페이지 10개, 두 번째 페이지 7개
            assertThat(secondPage.getBody().getPosts()).hasSize(7);
            assertThat(secondPage.getBody().isHasNext()).isFalse();
            assertThat(secondPage.getBody().getNextCursor()).isNull();

            // 프로필 정보는 매 페이지에 포함
            assertThat(secondPage.getBody().getUser().getUserId()).isEqualTo(SAME_TEAM_USER_ID);
        }

        @Test
        @DisplayName("삭제된 게시글은 조회되지 않음")
        void getUserPosts_excludesDeletedPosts() {
            // when - 전체 페이지 조회
            ResponseEntity<UserPostListResponse> firstPage = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            ResponseEntity<UserPostListResponse> secondPage = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts?cursor=" + firstPage.getBody().getNextCursor(),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            // then - 삭제된 게시글(id=400)이 포함되지 않음
            assertThat(firstPage.getBody().getPosts()).noneMatch(p -> p.getPostId() == 400L);
            assertThat(secondPage.getBody().getPosts()).noneMatch(p -> p.getPostId() == 400L);
        }

        @Test
        @DisplayName("게시글 상세 정보 포함 (이미지, 해시태그, 감정표현)")
        void getUserPosts_containsDetails() {
            // when - post 103은 두 번째 페이지에 있음 (최신순 정렬)
            ResponseEntity<UserPostListResponse> firstPage = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            ResponseEntity<UserPostListResponse> secondPage = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts?cursor=" + firstPage.getBody().getNextCursor(),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            // then - post 103 찾기 (이미지, 해시태그 있음)
            UserPostListResponse.PostSummary post103 = secondPage.getBody().getPosts().stream()
                    .filter(p -> p.getPostId() == 103L)
                    .findFirst()
                    .orElse(null);

            assertThat(post103).isNotNull();
            assertThat(post103.getContent()).isEqualTo("같은팀 DOOSAN 게시글 1");
            assertThat(post103.getChannel()).isEqualTo("DOOSAN");
            assertThat(post103.getImages()).hasSize(1);
            assertThat(post103.getHashtags()).containsExactlyInAnyOrder("야구", "두산");
            assertThat(post103.getEmotions().getLikeCount()).isEqualTo(10);
            assertThat(post103.getAuthor().getUserId()).isEqualTo(SAME_TEAM_USER_ID);
        }

        @Test
        @DisplayName("최신순 정렬 (게시글 ID 내림차순)")
        void getUserPosts_sortedByIdDesc() {
            // when
            ResponseEntity<UserPostListResponse> response = restTemplate.exchange(
                    "/api/v1/users/" + SAME_TEAM_USER_ID + "/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    UserPostListResponse.class
            );

            // then - ID 내림차순 확인
            var posts = response.getBody().getPosts();
            for (int i = 0; i < posts.size() - 1; i++) {
                assertThat(posts.get(i).getPostId()).isGreaterThan(posts.get(i + 1).getPostId());
            }
        }
    }
}
