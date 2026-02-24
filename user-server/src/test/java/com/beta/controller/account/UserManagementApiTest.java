package com.beta.controller.account;

import com.beta.ApiTestBase;
import com.beta.controller.account.request.UpdateBioRequest;
import com.beta.controller.account.response.UpdateBioResponse;
import com.beta.controller.account.response.WithdrawResponse;
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
@DisplayName("사용자 관리 API 통합 테스트")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/user-management-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserManagementApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long USER_WITH_BIO = 1L;
    private static final Long USER_WITHOUT_BIO = 2L;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenProvider.generateAccessToken(USER_WITH_BIO, "DOOSAN", "USER");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/me/bio")
    class UpdateBio {

        @Test
        @DisplayName("한줄 소개 수정 성공")
        void updateBio_success() {
            // given
            UpdateBioRequest request = new UpdateBioRequest();
            setField(request, "bio", "새로운 한줄소개");

            // when
            ResponseEntity<UpdateBioResponse> response = restTemplate.exchange(
                    "/api/v1/users/me/bio",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request, createAuthHeaders()),
                    UpdateBioResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getBio()).isEqualTo("새로운 한줄소개");
            assertThat(response.getBody().getMessage()).isEqualTo("한줄 소개가 수정되었습니다");
        }

        @Test
        @DisplayName("빈 문자열로 한줄 소개 삭제")
        void updateBio_emptyString_deletesBio() {
            // given
            UpdateBioRequest request = new UpdateBioRequest();
            setField(request, "bio", "");

            // when
            ResponseEntity<UpdateBioResponse> response = restTemplate.exchange(
                    "/api/v1/users/me/bio",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request, createAuthHeaders()),
                    UpdateBioResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getBio()).isNull();
        }

        @Test
        @DisplayName("null로 한줄 소개 삭제")
        void updateBio_null_deletesBio() {
            // given
            UpdateBioRequest request = new UpdateBioRequest();

            // when
            ResponseEntity<UpdateBioResponse> response = restTemplate.exchange(
                    "/api/v1/users/me/bio",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request, createAuthHeaders()),
                    UpdateBioResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getBio()).isNull();
        }

        @Test
        @DisplayName("50자 초과 한줄 소개 - 400 에러")
        void updateBio_tooLong_returns400() {
            // given
            UpdateBioRequest request = new UpdateBioRequest();
            setField(request, "bio", "a".repeat(51));

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    "/api/v1/users/me/bio",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request, createAuthHeaders()),
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("인증 없이 요청 - 401 에러")
        void updateBio_unauthorized_returns401() {
            // given
            UpdateBioRequest request = new UpdateBioRequest();
            setField(request, "bio", "테스트");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/users/me/bio",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request, headers),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("50자 한줄 소개 - 성공")
        void updateBio_exactly50chars_success() {
            // given
            String bio50 = "a".repeat(50);
            UpdateBioRequest request = new UpdateBioRequest();
            setField(request, "bio", bio50);

            // when
            ResponseEntity<UpdateBioResponse> response = restTemplate.exchange(
                    "/api/v1/users/me/bio",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request, createAuthHeaders()),
                    UpdateBioResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getBio()).isEqualTo(bio50);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/me")
    class Withdraw {

        @Test
        @DisplayName("계정 탈퇴 요청 성공")
        void withdraw_success() {
            // when
            ResponseEntity<WithdrawResponse> response = restTemplate.exchange(
                    "/api/v1/users/me",
                    HttpMethod.DELETE,
                    new HttpEntity<>(createAuthHeaders()),
                    WithdrawResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("탈퇴 요청이 처리되었습니다");
            assertThat(response.getBody().getWithdrawnAt()).isNotNull();
            assertThat(response.getBody().getScheduledDeletionAt()).isNotNull();
            assertThat(response.getBody().getScheduledDeletionAt())
                    .isEqualTo(response.getBody().getWithdrawnAt().plusDays(30));
        }

        @Test
        @DisplayName("인증 없이 요청 - 401 에러")
        void withdraw_unauthorized_returns401() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/users/me",
                    HttpMethod.DELETE,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
