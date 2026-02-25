package com.beta.controller.home;

import com.beta.ApiTestBase;
import com.beta.controller.home.response.HomeResponse;
import com.beta.core.infra.client.kbo.KboRankingClient;
import com.beta.core.infra.client.kbo.TeamRanking;
import com.beta.core.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("홈 화면 API 통합 테스트")
@Sql(scripts = "/sql/home-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class HomeApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private KboRankingClient kboRankingClient;

    private static final Long TEST_USER_ID = 1L;
    private static final Long BLOCKED_USER_ID = 3L;

    private String accessToken;

    private static final List<TeamRanking> MOCK_RANKINGS = List.of(
            new TeamRanking(1, "삼성", 144, 87, 55, 2, "0.613", "-", "7승3패", "3연승"),
            new TeamRanking(2, "LG", 144, 83, 59, 2, "0.585", "4.0", "6승4패", "1연승"),
            new TeamRanking(3, "두산", 144, 80, 62, 2, "0.563", "7.0", "5승5패", "2연패")
    );

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, "DOOSAN", "USER");
        when(kboRankingClient.getRankings()).thenReturn(MOCK_RANKINGS);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("GET /api/v1/home")
    class GetHome {

        @Test
        @DisplayName("홈 화면 데이터를 조회한다")
        void getHome_success() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTeamRankings()).isNotEmpty();
            assertThat(response.getBody().getPopularPosts()).isNotEmpty();
        }

        @Test
        @DisplayName("KBO 팀 순위가 정확히 반환된다")
        void getHome_teamRankings() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then
            assertThat(response.getBody().getTeamRankings()).hasSize(3);

            HomeResponse.TeamRankingResponse first = response.getBody().getTeamRankings().get(0);
            assertThat(first.getRank()).isEqualTo(1);
            assertThat(first.getTeamName()).isEqualTo("삼성");
            assertThat(first.getWins()).isEqualTo(87);
            assertThat(first.getLosses()).isEqualTo(55);
            assertThat(first.getWinRate()).isEqualTo("0.613");
        }

        @Test
        @DisplayName("인기 게시글이 감정 총합 기준 내림차순으로 반환된다")
        void getHome_popularPostsSortedByEmotions() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - 감정 총합 기준 내림차순 (100: 25, 101: 11, 102: 5)
            assertThat(response.getBody().getPopularPosts()).hasSize(3);
            assertThat(response.getBody().getPopularPosts().get(0).getPostId()).isEqualTo(100L);
            assertThat(response.getBody().getPopularPosts().get(1).getPostId()).isEqualTo(101L);
            assertThat(response.getBody().getPopularPosts().get(2).getPostId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("ALL 채널 게시글만 조회된다")
        void getHome_onlyAllChannelPosts() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - 모든 인기 게시글이 ALL 채널
            assertThat(response.getBody().getPopularPosts())
                    .allMatch(p -> p.getChannel().equals("ALL"));
            // DOOSAN 채널 게시글(105)이 없어야 함
            assertThat(response.getBody().getPopularPosts())
                    .noneMatch(p -> p.getPostId() == 105L);
        }

        @Test
        @DisplayName("차단한 사용자의 게시글은 제외된다")
        void getHome_excludesBlockedUserPosts() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - user 3 (차단유저)의 게시글(id=103)이 없어야 함
            assertThat(response.getBody().getPopularPosts())
                    .noneMatch(p -> p.getAuthor().getUserId().equals(BLOCKED_USER_ID));
            assertThat(response.getBody().getPopularPosts())
                    .noneMatch(p -> p.getPostId() == 103L);
        }

        @Test
        @DisplayName("24시간 이전 게시글은 제외된다")
        void getHome_excludesOldPosts() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - 30시간 전 게시글(id=104)이 없어야 함
            assertThat(response.getBody().getPopularPosts())
                    .noneMatch(p -> p.getPostId() == 104L);
        }

        @Test
        @DisplayName("삭제된 게시글은 제외된다")
        void getHome_excludesDeletedPosts() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - 삭제된 게시글(id=106)이 없어야 함
            assertThat(response.getBody().getPopularPosts())
                    .noneMatch(p -> p.getPostId() == 106L);
        }

        @Test
        @DisplayName("인기 게시글의 상세 정보가 정확히 반환된다")
        void getHome_popularPostDetails() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - post 100의 상세 정보 검증
            HomeResponse.PopularPostResponse post100 = response.getBody().getPopularPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getContent()).isEqualTo("오늘 경기 대박이었다!");
            assertThat(post100.getCommentCount()).isEqualTo(15);
            assertThat(post100.getEmotions().getLikeCount()).isEqualTo(10);
            assertThat(post100.getEmotions().getSadCount()).isEqualTo(2);
            assertThat(post100.getEmotions().getFunCount()).isEqualTo(5);
            assertThat(post100.getEmotions().getHypeCount()).isEqualTo(8);
        }

        @Test
        @DisplayName("인기 게시글의 이미지 정보가 반환된다")
        void getHome_popularPostImages() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - post 100의 이미지 검증
            HomeResponse.PopularPostResponse post100 = response.getBody().getPopularPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getImages()).hasSize(1);
            assertThat(post100.getImages().get(0).getImageUrl())
                    .isEqualTo("https://storage.example.com/images/post100_1.jpg");
        }

        @Test
        @DisplayName("인기 게시글의 해시태그 정보가 반환된다")
        void getHome_popularPostHashtags() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - post 100의 해시태그 검증
            HomeResponse.PopularPostResponse post100 = response.getBody().getPopularPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getHashtags()).hasSize(2);
            assertThat(post100.getHashtags()).containsExactlyInAnyOrder("야구", "KBO");
        }

        @Test
        @DisplayName("인기 게시글의 작성자 정보가 반환된다")
        void getHome_popularPostAuthor() {
            // when
            ResponseEntity<HomeResponse> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    HomeResponse.class
            );

            // then - post 100의 작성자 정보 검증
            HomeResponse.PopularPostResponse post100 = response.getBody().getPopularPosts().stream()
                    .filter(p -> p.getPostId() == 100L)
                    .findFirst()
                    .orElseThrow();

            assertThat(post100.getAuthor().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(post100.getAuthor().getNickname()).isEqualTo("테스트유저");
            assertThat(post100.getAuthor().getTeamCode()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("인증 없이 요청하면 401 에러")
        void getHome_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/home",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
