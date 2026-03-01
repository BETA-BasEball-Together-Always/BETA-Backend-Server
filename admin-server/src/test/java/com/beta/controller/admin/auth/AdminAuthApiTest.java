package com.beta.controller.admin.auth;

import com.beta.account.domain.service.AdminRefreshTokenService;
import com.beta.account.domain.service.SocialUserInfoService;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.account.infra.client.apple.AppleLoginClient;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.exception.account.InvalidSocialTokenException;
import com.beta.core.exception.account.SocialApiException;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.data.redis.username=",
                "spring.data.redis.password="
        }
)
@Sql(scripts = {"/sql/admin-cleanup.sql", "/sql/admin-auth-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminAuthApiTest extends MysqlRedisTestContainer {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AdminRefreshTokenService adminRefreshTokenService;

    @MockitoBean
    private SocialUserInfoService socialUserInfoService;

    // @SpringBootTest 컨텍스트 로딩 시 AppleLoginClient 빈도 함께 생성되어서 oauth.apple.* 프로퍼티 의존성을 모킹함
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
    void 관리자_카카오_로그인시_200_성공_응답을_반환한다() {
        // given
        given(socialUserInfoService.fetchSocialUserInfo(eq("kakao-admin-token"), any()))
                .willReturn(SocialUserInfo.builder()
                        .socialId("admin-social-id")
                        .email("admin@test.com")
                        .build());

        // when
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", "kakao-admin-token"),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("accessToken", "user");
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains(AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME + "=");
    }

    @Test
    void 관리자_카카오_로그인시_일반유저면_403_ADMIN001_예외를_반환한다() {
        // given
        given(socialUserInfoService.fetchSocialUserInfo(eq("kakao-user-token"), any()))
                .willReturn(SocialUserInfo.builder()
                        .socialId("normal-social-id")
                        .email("user@test.com")
                        .build());

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", "kakao-user-token"),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
    }

    @Test
    void 관리자_카카오_로그인시_탈퇴계정이면_403_USER002_예외를_반환한다() {
        // given
        given(socialUserInfoService.fetchSocialUserInfo(eq("kakao-withdrawn-token"), any()))
                .willReturn(SocialUserInfo.builder()
                        .socialId("admin-withdrawn-social-id")
                        .email("admin-withdrawn@test.com")
                        .build());

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", "kakao-withdrawn-token"),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER002");
    }

    @Test
    void 관리자_카카오_로그인시_정지계정이면_403_USER003_예외를_반환한다() {
        // given
        given(socialUserInfoService.fetchSocialUserInfo(eq("kakao-suspended-token"), any()))
                .willReturn(SocialUserInfo.builder()
                        .socialId("admin-suspended-social-id")
                        .email("admin-suspended@test.com")
                        .build());

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", "kakao-suspended-token"),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER003");
    }

    @Test
    void 관리자_카카오_로그인시_소셜토큰이_유효하지_않으면_401_SOCIAL001_예외를_반환한다() {
        // given
        given(socialUserInfoService.fetchSocialUserInfo(eq("kakao-invalid-token"), any()))
                .willThrow(new InvalidSocialTokenException("invalid social token"));

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", "kakao-invalid-token"),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("SOCIAL001");
    }

    @Test
    void 관리자_카카오_로그인시_소셜_API_오류면_500_SOCIAL002_예외를_반환한다() {
        // given
        given(socialUserInfoService.fetchSocialUserInfo(eq("kakao-social-api-error-token"), any()))
                .willThrow(new SocialApiException("social api error"));

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", "kakao-social-api-error-token"),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("SOCIAL002");
    }

    @Test
    void 관리자_카카오_로그인시_토큰이_비어있으면_400_VALIDATION001_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/admin/auth/login/kakao",
                Map.of("token", ""),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION001");
    }

    @Test
    void 관리자_API를_토큰없이_호출하면_401_JWT002_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/api/v1/admin/me", ErrorResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void CLIENT_클레임이_없는_ADMIN_토큰으로_호출시_401_JWT002_예외를_반환한다() {
        // given
        String userStyleAdminToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStyleAdminToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void CLIENT는_ADMIN이고_ROLE이_USER인_토큰으로_호출시_403_ADMIN001_예외를_반환한다() {
        // given
        String userRoleToken = jwtTokenProvider.generateAccessToken(
                2L,
                null,
                "USER",
                AdminAuthConstants.ADMIN_CLIENT
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userRoleToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
    }

    @Test
    void 관리자_리프레시_토큰_재발급시_200_성공_응답을_반환한다() {
        // given
        String refreshToken = "admin-refresh-success-" + UUID.randomUUID();
        adminRefreshTokenService.upsertRefreshToken(1L, refreshToken);
        HttpHeaders headers = refreshCookieHeader(refreshToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("accessToken");
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains(AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME + "=");
    }

    @Test
    void 관리자_리프레시_쿠키가_없으면_401_JWT002_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                HttpEntity.EMPTY,
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void 관리자_리프레시_토큰이_유효하지_않으면_401_JWT002_예외를_반환한다() {
        // given
        HttpHeaders headers = refreshCookieHeader("admin-refresh-invalid-" + UUID.randomUUID());

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void 관리자_리프레시_대상_사용자가_없으면_404_USER001_예외를_반환한다() {
        // given
        String refreshToken = "admin-refresh-not-found-" + UUID.randomUUID();
        adminRefreshTokenService.upsertRefreshToken(999L, refreshToken);
        HttpHeaders headers = refreshCookieHeader(refreshToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER001");
    }

    @Test
    void 관리자_리프레시_대상_사용자가_일반유저면_403_ADMIN001_예외를_반환한다() {
        // given
        String refreshToken = "admin-refresh-user-role-" + UUID.randomUUID();
        adminRefreshTokenService.upsertRefreshToken(2L, refreshToken);
        HttpHeaders headers = refreshCookieHeader(refreshToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
    }

    @Test
    void 관리자_리프레시_대상_사용자가_탈퇴계정이면_403_USER002_예외를_반환한다() {
        // given
        String refreshToken = "admin-refresh-withdrawn-" + UUID.randomUUID();
        adminRefreshTokenService.upsertRefreshToken(3L, refreshToken);
        HttpHeaders headers = refreshCookieHeader(refreshToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER002");
    }

    @Test
    void 관리자_리프레시_대상_사용자가_정지계정이면_403_USER003_예외를_반환한다() {
        // given
        String refreshToken = "admin-refresh-suspended-" + UUID.randomUUID();
        adminRefreshTokenService.upsertRefreshToken(4L, refreshToken);
        HttpHeaders headers = refreshCookieHeader(refreshToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER003");
    }

    private HttpHeaders refreshCookieHeader(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
                HttpHeaders.COOKIE,
                AdminAuthConstants.REFRESH_TOKEN_COOKIE_NAME + "=" + refreshToken
        );
        return headers;
    }
}
