package com.beta.controller.user;

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
@Sql(scripts = {"/sql/admin-user-cleanup.sql", "/sql/admin-user-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-user-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserControllerApiTest extends MysqlRedisTestContainer {

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
    void 관리자_사용자목록_조회시_200_응답과_검색된_페이지_데이터를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/users?page=0&size=10&status=ACTIVE&keyword=slugger",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(toLong(response.getBody().get("totalCount"))).isEqualTo(2L);
        assertThat(toInt(response.getBody().get("page"))).isEqualTo(0);
        assertThat(toInt(response.getBody().get("size"))).isEqualTo(10);

        List<Map<String, Object>> items = toListOfMap(response.getBody().get("items"));
        assertThat(items).hasSize(2);
        assertThat(items.get(0).get("nickname")).isEqualTo("slugger5");
        assertThat(items.get(0).get("email")).isEqualTo("slugger5@test.com");
        assertThat(items.get(0).get("socialProvider")).isEqualTo("KAKAO");
        assertThat(items.get(0).get("favoriteTeamName")).isNull();
        assertThat(items.get(0).get("gender")).isEqualTo("F");
        assertThat(toInt(items.get(0).get("age"))).isEqualTo(24);
        assertThat(items.get(0).get("bio")).isEqualTo("잠실 직관파");
        assertThat(items.get(0).get("status")).isEqualTo("ACTIVE");
        assertThat(items.get(1).get("nickname")).isEqualTo("slugger2");
        assertThat(items.get(1).get("favoriteTeamName")).isEqualTo("LG 트윈스");
        assertThat(items.get(1).get("gender")).isEqualTo("M");
        assertThat(toInt(items.get(1).get("age"))).isEqualTo(28);
        assertThat(items.get(1).get("bio")).isEqualTo("LG 팬입니다");
    }

    @Test
    void 관리자_사용자목록_조회시_관리자_계정은_제외한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/users?page=0&size=10",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(toLong(response.getBody().get("totalCount"))).isEqualTo(5L);

        List<Map<String, Object>> items = toListOfMap(response.getBody().get("items"));
        assertThat(items).extracting(item -> item.get("nickname"))
                .doesNotContain("admin-user");
    }

    @Test
    void 관리자_사용자목록_API를_토큰없이_호출하면_401_JWT002_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/v1/admin/users",
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void CLIENT는_ADMIN이고_ROLE이_USER인_토큰으로_사용자목록을_호출하면_403_ADMIN001_예외를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(2L, null, "USER", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
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

    private long toLong(Object value) {
        return ((Number) value).longValue();
    }

    private int toInt(Object value) {
        return ((Number) value).intValue();
    }
}
