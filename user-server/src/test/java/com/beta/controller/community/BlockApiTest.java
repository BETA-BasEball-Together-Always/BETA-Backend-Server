package com.beta.controller.community;

import com.beta.ApiTestBase;
import com.beta.controller.community.response.BlockResponse;
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
@DisplayName("사용자 차단 API 통합 테스트")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/comment-crud-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class BlockApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long TEST_USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long BLOCKED_USER_ID = 3L;  // 이미 차단된 사용자

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
    @DisplayName("POST /api/v1/community/users/{userId}/block")
    class BlockUser {

        @Test
        @DisplayName("새 사용자 차단 성공")
        void blockUser_success() {
            // given - user 2는 아직 차단 안 됨

            // when
            ResponseEntity<BlockResponse> response = restTemplate.exchange(
                    "/api/v1/community/users/" + OTHER_USER_ID + "/block",
                    HttpMethod.POST,
                    new HttpEntity<>(createAuthHeaders()),
                    BlockResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getBody().getBlockedUserId()).isEqualTo(OTHER_USER_ID);
            assertThat(response.getBody().isBlocked()).isTrue();
        }

        @Test
        @DisplayName("이미 차단된 사용자 차단 시 409")
        void blockUser_alreadyBlocked_conflict() {
            // given - user 3은 이미 차단됨

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/users/" + BLOCKED_USER_ID + "/block",
                    HttpMethod.POST,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("자기 자신 차단 시 400")
        void blockUser_self_badRequest() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/users/" + TEST_USER_ID + "/block",
                    HttpMethod.POST,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("인증 없이 차단 시 401")
        void blockUser_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/users/" + OTHER_USER_ID + "/block",
                    HttpMethod.POST,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/community/users/{userId}/block")
    class UnblockUser {

        @Test
        @DisplayName("차단 해제 성공")
        void unblockUser_success() {
            // given - user 3은 이미 차단됨

            // when
            ResponseEntity<BlockResponse> response = restTemplate.exchange(
                    "/api/v1/community/users/" + BLOCKED_USER_ID + "/block",
                    HttpMethod.DELETE,
                    new HttpEntity<>(createAuthHeaders()),
                    BlockResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getBody().getBlockedUserId()).isEqualTo(BLOCKED_USER_ID);
            assertThat(response.getBody().isBlocked()).isFalse();
        }

        @Test
        @DisplayName("차단하지 않은 사용자 해제 시 404")
        void unblockUser_notBlocked_notFound() {
            // given - user 2는 차단 안 됨

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/users/" + OTHER_USER_ID + "/block",
                    HttpMethod.DELETE,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("인증 없이 차단 해제 시 401")
        void unblockUser_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/users/" + BLOCKED_USER_ID + "/block",
                    HttpMethod.DELETE,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
