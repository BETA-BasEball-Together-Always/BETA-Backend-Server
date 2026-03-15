package com.beta.controller.dashboard;

import com.beta.account.infra.client.apple.AppleLoginClient;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.response.ErrorResponse;
import com.beta.core.security.AdminAuthConstants;
import com.beta.core.security.JwtTokenProvider;
import com.beta.docker.MysqlRedisTestContainer;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.data.redis.username=",
                "spring.data.redis.password=",
                "management.health.mail.enabled=false"
        }
)
@Sql(scripts = {"/sql/admin-dashboard-cleanup.sql", "/sql/admin-dashboard-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-dashboard-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminDashboardApiTest extends MysqlRedisTestContainer {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AppleLoginClient appleLoginClient;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private AbstractAuthenticationDetailsProvider authProvider;

    @MockitoBean
    private ObjectStorage objectStorage;

    @MockitoBean
    private OracleCloudStorageClient oracleCloudStorageClient;

    @Test
    void 관리자_대시보드_조회시_200_응답과_전체_데이터를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(
                1L,
                null,
                "ADMIN",
                AdminAuthConstants.ADMIN_CLIENT
        );
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> body = response.getBody();
        assertThat(toLong(body.get("totalUserCount"))).isEqualTo(3L);
        assertThat(toLong(body.get("totalUserDelta"))).isEqualTo(1L);
        assertThat(toLong(body.get("todayPostCount"))).isEqualTo(3L);
        assertThat(toLong(body.get("todayPostDelta"))).isEqualTo(1L);
        assertThat(toLong(body.get("todayNewSignupCount"))).isEqualTo(2L);
        assertThat(toLong(body.get("todayNewSignupDelta"))).isEqualTo(1L);
        assertThat(toLong(body.get("pendingReportCount"))).isEqualTo(0L);

        List<Map<String, Object>> realtimeFeeds = toListOfMap(body.get("realtimeFeeds"));
        assertThat(realtimeFeeds).hasSize(5);

        Map<String, Object> post101 = findFeedByPostId(realtimeFeeds, 101L);
        assertThat(post101.get("authorNickname")).isEqualTo("slugger2");
        assertThat(post101.get("thumbnailUrl")).isEqualTo("https://cdn.beta.test/post101-thumb.jpg");

        Map<String, Object> post102 = findFeedByPostId(realtimeFeeds, 102L);
        assertThat(post102.get("thumbnailUrl")).isNull();

        Map<String, Object> post103 = findFeedByPostId(realtimeFeeds, 103L);
        assertThat(post103.get("thumbnailUrl")).isEqualTo("https://cdn.beta.test/post103-thumb.jpg");

        List<Map<String, Object>> popularTopics = toListOfMap(body.get("popularTopics"));
        assertThat(popularTopics).hasSize(3);
        assertThat(popularTopics.get(0).get("hashtag")).isEqualTo("오늘의경기");
        assertThat(toLong(popularTopics.get(0).get("usageCount"))).isEqualTo(234L);
    }

    @Test
    void 관리자_대시보드_API를_토큰없이_호출하면_401_JWT002_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/v1/admin/dashboard",
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    private HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toListOfMap(Object value) {
        return ((List<Object>) value).stream()
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    private Map<String, Object> findFeedByPostId(List<Map<String, Object>> feeds, Long postId) {
        return feeds.stream()
                .filter(feed -> toLong(feed.get("postId")) == postId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("feed not found postId=" + postId));
    }

    private long toLong(Object value) {
        return ((Number) value).longValue();
    }
}
