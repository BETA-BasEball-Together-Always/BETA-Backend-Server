package com.beta.controller.action;

import com.beta.account.domain.entity.User;
import com.beta.account.infra.client.apple.AppleLoginClient;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.CommentJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.response.ErrorResponse;
import com.beta.core.security.AdminAuthConstants;
import com.beta.core.security.JwtTokenProvider;
import com.beta.domain.entity.AdminLog;
import com.beta.infra.repository.AdminLogJpaRepository;
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

import java.time.LocalDate;
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
@Sql(scripts = {"/sql/admin-action-cleanup.sql", "/sql/admin-action-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-action-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminActionApiTest extends MysqlRedisTestContainer {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PostJpaRepository postJpaRepository;

    @Autowired
    CommentJpaRepository commentJpaRepository;

    @Autowired
    AdminLogJpaRepository adminLogJpaRepository;

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
    void 관리자_로그_조회_API_호출시_필터조건에_맞는_페이지결과를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        adminLogJpaRepository.save(AdminLog.suspendMember(1L, 2L, "운영 정책 위반"));
        adminLogJpaRepository.save(AdminLog.hidePost(1L, 101L, "운영자 숨김"));
        adminLogJpaRepository.save(AdminLog.hideComment(1L, 201L, "댓글 숨김"));

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/action-logs?page=0&size=10&action=MEMBER_SUSPEND",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Long.parseLong(String.valueOf(response.getBody().get("totalCount")))).isEqualTo(1L);
        assertThat(Integer.parseInt(String.valueOf(response.getBody().get("page")))).isEqualTo(0);
        assertThat(Integer.parseInt(String.valueOf(response.getBody().get("size")))).isEqualTo(10);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).get("actorAdminId")).isEqualTo(1);
        assertThat(items.get(0).get("actorAdminNickname")).isEqualTo("admin-user");
        assertThat(items.get(0).get("action")).isEqualTo("MEMBER_SUSPEND");
        assertThat(items.get(0).get("targetType")).isEqualTo("MEMBER");
        assertThat(items.get(0).get("reason")).isEqualTo("운영 정책 위반");
    }

    @Test
    void 관리자_로그_조회_API_호출시_조회_시작일이_종료일보다_늦으면_400_VALIDATION001_예외를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/action-logs?from=%s&to=%s".formatted(LocalDate.now(), LocalDate.now().minusDays(1)),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION001");
    }

    @Test
    void 관리자_회원_정지_API_호출시_200_응답과_상태변경을_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/members/2/suspend",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("reason", "운영 정책 위반"), headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("회원이 정지되었습니다.");

        User user = userJpaRepository.findById(2L).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.SUSPENDED);
        assertThat(adminLogJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void 관리자_게시글_다시노출_API_호출시_200_응답과_상태변경을_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/posts/102/unhide",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("reason", "숨김 해제"), headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("게시글 숨김이 해제되었습니다.");

        Post post = postJpaRepository.findById(102L).orElseThrow();
        assertThat(post.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(adminLogJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void 관리자_댓글_숨김_API_호출시_200_응답과_상태변경을_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/comments/201/hide",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("reason", "운영자 숨김"), headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("댓글이 숨김 처리되었습니다.");

        Comment comment = commentJpaRepository.findById(201L).orElseThrow();
        assertThat(comment.getStatus()).isEqualTo(Status.HIDDEN);
        assertThat(adminLogJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void 관리자_액션_API를_토큰없이_호출하면_401_JWT002_예외를_반환한다() {
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/members/2/suspend",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("reason", "운영 정책 위반")),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JWT002");
    }

    @Test
    void CLIENT는_ADMIN이고_ROLE이_USER인_토큰으로_관리자_액션_API를_호출하면_403_ADMIN001_예외를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(2L, null, "USER", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/members/2/suspend",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("reason", "운영 정책 위반"), headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
    }

    @Test
    void 관리자_액션_API_호출시_사유가_비어있으면_400_VALIDATION001_예외를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "ADMIN", AdminAuthConstants.ADMIN_CLIENT);
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/members/2/suspend",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("reason", ""), headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION001");
    }

    private HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
