package com.beta.controller.account;

import com.beta.ApiTestBase;
import com.beta.account.application.port.CommunityDataCleanupPort;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.community.infra.repository.*;
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

    @Autowired
    private CommunityDataCleanupPort communityDataCleanupPort;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private CommentJpaRepository commentJpaRepository;

    @Autowired
    private CommentLikeJpaRepository commentLikeJpaRepository;

    @Autowired
    private EmotionJpaRepository emotionJpaRepository;

    @Autowired
    private PostImageJpaRepository postImageJpaRepository;

    @Autowired
    private PostHashtagJpaRepository postHashtagJpaRepository;

    @Autowired
    private UserBlockJpaRepository userBlockJpaRepository;

    private static final Long USER_WITH_BIO = 1L;
    private static final Long CLEANUP_TEST_USER = 4L;

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

    @Nested
    @DisplayName("CommunityDataCleanupPort 테스트")
    class DataCleanup {

        @Test
        @DisplayName("사용자 데이터 삭제 - 게시글/댓글/좋아요/감정표현/이미지/해시태그/차단 모두 삭제")
        void deleteAllUserCommunityData_deletesAllRelatedData() {
            // given - 삭제 전 데이터 확인
            assertThat(postJpaRepository.findById(100L)).isPresent();
            assertThat(postImageJpaRepository.findById(1L)).isPresent();
            assertThat(commentJpaRepository.findById(1L)).isPresent(); // 다른 유저가 쓴 댓글
            assertThat(commentJpaRepository.findById(2L)).isPresent(); // 본인이 쓴 댓글
            assertThat(commentJpaRepository.findById(3L)).isPresent(); // 다른 게시글에 쓴 댓글
            assertThat(commentLikeJpaRepository.findById(1L)).isPresent(); // user4가 누른 좋아요
            assertThat(commentLikeJpaRepository.findById(2L)).isPresent(); // user2가 댓글1에 누른 좋아요
            assertThat(emotionJpaRepository.findById(1L)).isPresent(); // user2가 게시글100에 누른 감정
            assertThat(emotionJpaRepository.findById(2L)).isPresent(); // user4가 게시글200에 누른 감정
            assertThat(postHashtagJpaRepository.findById(1L)).isPresent();
            assertThat(userBlockJpaRepository.findById(1L)).isPresent();

            // when - user4 커뮤니티 데이터 삭제
            communityDataCleanupPort.deleteAllUserCommunityData(CLEANUP_TEST_USER);

            // then - user4 관련 데이터 삭제 확인
            // 1. user4의 게시글(100) 삭제됨
            assertThat(postJpaRepository.findById(100L)).isEmpty();

            // 2. 게시글100의 이미지 삭제됨
            assertThat(postImageJpaRepository.findById(1L)).isEmpty();

            // 3. 게시글100에 달린 모든 댓글 삭제됨 (다른 유저가 쓴 것 포함)
            assertThat(commentJpaRepository.findById(1L)).isEmpty();
            assertThat(commentJpaRepository.findById(2L)).isEmpty();

            // 4. user4가 다른 게시글에 쓴 댓글 삭제됨
            assertThat(commentJpaRepository.findById(3L)).isEmpty();

            // 5. 게시글100의 댓글에 달린 좋아요 삭제됨 (다른 유저 것 포함)
            assertThat(commentLikeJpaRepository.findById(2L)).isEmpty();

            // 6. user4가 누른 댓글 좋아요 삭제됨
            assertThat(commentLikeJpaRepository.findById(1L)).isEmpty();

            // 7. 게시글100에 달린 감정표현 삭제됨 (다른 유저 것 포함)
            assertThat(emotionJpaRepository.findById(1L)).isEmpty();

            // 8. user4가 다른 게시글에 누른 감정표현 삭제됨
            assertThat(emotionJpaRepository.findById(2L)).isEmpty();

            // 9. 게시글100의 해시태그 연결 삭제됨
            assertThat(postHashtagJpaRepository.findById(1L)).isEmpty();

            // 10. user4의 차단 관계 삭제됨
            assertThat(userBlockJpaRepository.findById(1L)).isEmpty();

            // 11. 다른 유저의 게시글(200)은 유지됨
            assertThat(postJpaRepository.findById(200L)).isPresent();
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
