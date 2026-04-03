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
    void 관리자_사용자목록_조회시_200_응답과_성별_나이_제외된_페이지_데이터를_반환한다() {
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
        assertThat(items.get(0).get("bio")).isNotNull();
        assertThat(items.get(0).get("status")).isEqualTo("ACTIVE");
        assertThat(items.get(0)).doesNotContainKeys("gender", "age");

        assertThat(items.get(1).get("nickname")).isEqualTo("slugger2");
        assertThat(String.valueOf(items.get(1).get("favoriteTeamName"))).contains("LG");
        assertThat(items.get(1).get("bio")).isNotNull();
        assertThat(items.get(1)).doesNotContainKeys("gender", "age");
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
    @Sql(
            scripts = {"/sql/admin-user-test-data.sql", "/sql/admin-user-statistics-test-data.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    void 관리자_사용자통계_조회시_상태별_필터가_적용된_성별_나이_집계데이터를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/users/statistics?status=ACTIVE",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(toLong(response.getBody().get("totalUserCount"))).isEqualTo(9L);

        List<Map<String, Object>> genderStats = toListOfMap(response.getBody().get("genderStats"));
        assertThat(genderStats).hasSize(3);
        assertThat(toLong(findItemByKey(genderStats, "gender", "FEMALE").get("count"))).isEqualTo(2L);
        assertThat(toLong(findItemByKey(genderStats, "gender", "MALE").get("count"))).isEqualTo(4L);
        assertThat(toLong(findItemByKey(genderStats, "gender", "UNSPECIFIED").get("count"))).isEqualTo(3L);

        List<Map<String, Object>> ageStats = toListOfMap(response.getBody().get("ageStats"));
        assertThat(ageStats).hasSize(7);
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "TEENS").get("count"))).isEqualTo(2L);
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "TWENTIES").get("count"))).isEqualTo(2L);
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "THIRTIES").get("count"))).isEqualTo(1L);
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "FORTIES").get("count"))).isZero();
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "FIFTIES").get("count"))).isEqualTo(1L);
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "OTHERS").get("count"))).isEqualTo(2L);
        assertThat(toLong(findItemByKey(ageStats, "ageGroup", "UNSPECIFIED").get("count"))).isEqualTo(1L);
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
    void CLIENT는_ADMIN이고_ROLE은_USER인_토큰으로_사용자목록을_호출하면_403_ADMIN001_예외를_반환한다() {
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

    private Map<String, Object> findItemByKey(
            List<Map<String, Object>> items,
            String keyName,
            String expectedValue
    ) {
        return items.stream()
                .filter(item -> expectedValue.equals(item.get(keyName)))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("item not found key=" + keyName + ", value=" + expectedValue));
    }

    private long toLong(Object value) {
        return ((Number) value).longValue();
    }

    private int toInt(Object value) {
        return ((Number) value).intValue();
    }
}
