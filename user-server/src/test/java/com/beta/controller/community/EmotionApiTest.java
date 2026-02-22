package com.beta.controller.community;

import com.beta.ApiTestBase;
import com.beta.controller.community.response.EmotionResponse;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("감정표현 API 통합 테스트")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/comment-crud-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class EmotionApiTest extends ApiTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final Long TEST_USER_ID = 1L;
    private static final Long POST_WITH_EMOTIONS = 100L;  // user1: LIKE, user2: SAD
    private static final Long POST_WITHOUT_EMOTIONS = 101L;

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
    @DisplayName("POST /api/v1/community/posts/{postId}/emotions")
    class ToggleEmotion {

        @Test
        @DisplayName("새 감정표현 추가")
        void toggleEmotion_add() {
            // given - post 101에는 감정표현이 없음
            Map<String, Object> request = Map.of("emotionType", "LIKE");

            // when
            ResponseEntity<EmotionResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITHOUT_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    EmotionResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPostId()).isEqualTo(POST_WITHOUT_EMOTIONS);
            assertThat(response.getBody().getEmotionType()).isEqualTo("LIKE");
            assertThat(response.getBody().isToggled()).isTrue();
            assertThat(response.getBody().getEmotions().getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 감정표현 제거 (같은 감정 다시 클릭)")
        void toggleEmotion_remove() {
            // given - user 1이 post 100에 LIKE를 이미 함
            Map<String, Object> request = Map.of("emotionType", "LIKE");

            // when
            ResponseEntity<EmotionResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITH_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    EmotionResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isToggled()).isFalse();
            assertThat(response.getBody().getEmotions().getLikeCount()).isEqualTo(0); // 1 -> 0
        }

        @Test
        @DisplayName("감정표현 변경 (다른 감정으로)")
        void toggleEmotion_change() {
            // given - user 1이 post 100에 LIKE를 이미 함, SAD로 변경
            Map<String, Object> request = Map.of("emotionType", "SAD");

            // when
            ResponseEntity<EmotionResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITH_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    EmotionResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEmotionType()).isEqualTo("SAD");
            assertThat(response.getBody().isToggled()).isTrue();
            // LIKE 감소 (1->0), SAD 증가 (1->2)
            assertThat(response.getBody().getEmotions().getLikeCount()).isEqualTo(0);
            assertThat(response.getBody().getEmotions().getSadCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("FUN 감정표현 추가")
        void toggleEmotion_addFun() {
            // given
            Map<String, Object> request = Map.of("emotionType", "FUN");

            // when
            ResponseEntity<EmotionResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITHOUT_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    EmotionResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getEmotionType()).isEqualTo("FUN");
            assertThat(response.getBody().getEmotions().getFunCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("HYPE 감정표현 추가")
        void toggleEmotion_addHype() {
            // given
            Map<String, Object> request = Map.of("emotionType", "HYPE");

            // when
            ResponseEntity<EmotionResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITHOUT_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    EmotionResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getEmotionType()).isEqualTo("HYPE");
            assertThat(response.getBody().getEmotions().getHypeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("잘못된 감정표현 타입 시 400")
        void toggleEmotion_invalidType_badRequest() {
            // given
            Map<String, Object> request = Map.of("emotionType", "INVALID");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITH_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 감정표현 시 404")
        void toggleEmotion_postNotFound() {
            // given
            Map<String, Object> request = Map.of("emotionType", "LIKE");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/99999/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, createAuthHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("인증 없이 감정표현 시 401")
        void toggleEmotion_unauthorized() {
            // given
            Map<String, Object> request = Map.of("emotionType", "LIKE");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts/" + POST_WITH_EMOTIONS + "/emotions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
