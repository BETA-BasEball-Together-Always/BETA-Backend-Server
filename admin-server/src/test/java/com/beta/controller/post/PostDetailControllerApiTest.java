package com.beta.controller.post;

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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.data.redis.username=",
                "spring.data.redis.password=",
                "management.health.mail.enabled=false"
        }
)
@Sql(scripts = {"/sql/admin-post-cleanup.sql", "/sql/admin-post-detail-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-post-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PostDetailControllerApiTest extends MysqlRedisTestContainer {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    AppleLoginClient appleLoginClient;

    @MockitoBean
    JavaMailSender javaMailSender;

    @MockitoBean
    AbstractAuthenticationDetailsProvider authProvider;

    @MockitoBean
    ObjectStorage objectStorage;

    @MockitoBean
    OracleCloudStorageClient oracleCloudStorageClient;

    @Test
    void 관리자_게시글상세_조회시_200_응답과_게시글상세_내용을_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/posts/200",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(toLong(response.getBody().get("postId"))).isEqualTo(200L);
        assertThat(response.getBody().get("content")).isEqualTo("관리자 상세 조회용 게시글");
        assertThat(response.getBody().get("channel")).isEqualTo("LG");
        assertThat(response.getBody().get("status")).isEqualTo("ACTIVE");
        assertThat(toInt(response.getBody().get("commentCount"))).isEqualTo(4);

        Map<String, Object> author = toMap(response.getBody().get("author"));
        assertThat(toLong(author.get("userId"))).isEqualTo(2L);
        assertThat(author.get("nickname")).isEqualTo("slugger2");
        assertThat(author.get("teamCode")).isEqualTo("LG");

        List<Map<String, Object>> comments = toListOfMap(response.getBody().get("comments"));
        assertThat(comments).hasSize(4);
    }

    @Test
    void 관리자_게시글상세_조회는_차단상태와_무관하게_댓글을_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/posts/200",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        List<Map<String, Object>> comments = toListOfMap(response.getBody().get("comments"));
        assertThat(comments)
                .extracting(comment -> toLong(comment.get("commentId")))
                .contains(203L);
    }

    @Test
    void 관리자_게시글상세_조회는_숨김상태_게시글을_조회할수있다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/posts/201",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(toLong(response.getBody().get("postId"))).isEqualTo(201L);
        assertThat(response.getBody().get("content")).isEqualTo("숨김 처리된 게시글");
        assertThat(response.getBody().get("status")).isEqualTo("HIDDEN");
    }

    @Test
    void 관리자_게시글댓글_추가조회시_200_응답과_댓글페이지를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/posts/200/comments?cursor=203",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("hasNext")).isEqualTo(false);

        List<Map<String, Object>> comments = toListOfMap(response.getBody().get("comments"));
        assertThat(comments)
                .extracting(comment -> toLong(comment.get("commentId")))
                .containsExactly(202L, 201L);
    }

    @Test
    void 관리자_게시글상세_API를_토큰없이_호출하면_401_JWT002_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/v1/admin/posts/200",
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void CLIENT는_ADMIN이고_ROLE이_USER인_토큰으로_게시글상세를_호출하면_403_ADMIN001_예외를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(4L, null, "USER", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/posts/200",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
    }

    HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> toListOfMap(Object value) {
        return ((List<Object>) value).stream()
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> toMap(Object value) {
        return (Map<String, Object>) value;
    }

    long toLong(Object value) {
        return ((Number) value).longValue();
    }

    int toInt(Object value) {
        return ((Number) value).intValue();
    }
}
