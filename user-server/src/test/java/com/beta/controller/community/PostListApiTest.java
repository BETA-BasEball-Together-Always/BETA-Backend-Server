package com.beta.controller.community;

import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.domain.entity.SignupStep;
import com.beta.account.domain.entity.User;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.infra.repository.BaseballTeamRepository;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.UserBlock;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.community.infra.repository.UserBlockJpaRepository;
import com.beta.ApiTestBase;
import com.beta.controller.community.response.PostListResponse;
import com.beta.core.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("게시글 목록 조회 API 통합 테스트")
class PostListApiTest extends ApiTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BaseballTeamRepository baseballTeamRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private UserBlockJpaRepository userBlockJpaRepository;

    private User testUser;
    private User otherUser;
    private User blockedUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        userBlockJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        BaseballTeam team = baseballTeamRepository.findById("DOOSAN").orElseGet(() ->
                baseballTeamRepository.save(BaseballTeam.builder()
                        .code("DOOSAN")
                        .teamNameEn("Doosan Bears")
                        .teamNameKr("두산 베어스")
                        .homeStadium("잠실야구장")
                        .stadiumAddress("서울시 송파구")
                        .build())
        );

        testUser = userJpaRepository.save(User.builder()
                .socialId("test123")
                .socialProvider(SocialProvider.KAKAO)
                .nickname("테스트유저")
                .email("test@test.com")
                .signupStep(SignupStep.COMPLETED)
                .baseballTeam(team)
                .build());

        otherUser = userJpaRepository.save(User.builder()
                .socialId("other123")
                .socialProvider(SocialProvider.KAKAO)
                .nickname("다른유저")
                .email("other@test.com")
                .signupStep(SignupStep.COMPLETED)
                .baseballTeam(team)
                .build());

        blockedUser = userJpaRepository.save(User.builder()
                .socialId("blocked123")
                .socialProvider(SocialProvider.KAKAO)
                .nickname("차단유저")
                .email("blocked@test.com")
                .signupStep(SignupStep.COMPLETED)
                .baseballTeam(team)
                .build());

        accessToken = jwtTokenProvider.generateAccessToken(testUser.getId(), "DOOSAN", "USER");
    }

    private Post createActivePost(Long userId, String channel, String content) {
        Post post = Post.builder()
                .userId(userId)
                .content(content)
                .channel(channel)
                .build();
        post.activate();
        return postJpaRepository.save(post);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("GET /api/v1/community/posts")
    class GetPostList {

        @Test
        @DisplayName("게시글 목록을 조회한다")
        void getPostList_success() {
            // given
            createActivePost(testUser.getId(), "ALL", "첫번째 게시글");
            createActivePost(testUser.getId(), "ALL", "두번째 게시글");
            createActivePost(otherUser.getId(), "ALL", "다른 유저 게시글");

            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(3);
        }

        @Test
        @DisplayName("차단한 사용자의 게시글은 제외된다")
        void getPostList_excludesBlockedUser() {
            // given
            createActivePost(testUser.getId(), "ALL", "내 게시글");
            createActivePost(blockedUser.getId(), "ALL", "차단유저 게시글");

            userBlockJpaRepository.save(UserBlock.builder()
                    .blockerId(testUser.getId())
                    .blockedId(blockedUser.getId())
                    .build());

            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);
            assertThat(response.getBody().getPosts().get(0).getContent()).isEqualTo("내 게시글");
        }

        @Test
        @DisplayName("채널 필터링이 동작한다")
        void getPostList_channelFilter() {
            // given
            createActivePost(testUser.getId(), "ALL", "전체 게시글");
            createActivePost(testUser.getId(), "DOOSAN", "두산 게시글");
            createActivePost(testUser.getId(), "LG", "LG 게시글");

            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?channel=DOOSAN",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);
            assertThat(response.getBody().getPosts().get(0).getChannel()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("커서 기반 페이징이 동작한다")
        void getPostList_cursorPaging() {
            // given
            Post post1 = createActivePost(testUser.getId(), "ALL", "게시글1");
            Post post2 = createActivePost(testUser.getId(), "ALL", "게시글2");
            Post post3 = createActivePost(testUser.getId(), "ALL", "게시글3");

            // when - post3 이후의 게시글 조회
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts?cursor=" + post3.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(2);
            assertThat(response.getBody().getPosts().get(0).getPostId()).isEqualTo(post2.getId());
            assertThat(response.getBody().getPosts().get(1).getPostId()).isEqualTo(post1.getId());
        }

        @Test
        @DisplayName("hasNext가 올바르게 반환된다")
        void getPostList_hasNext() {
            // given - 11개 게시글 생성 (PAGE_SIZE + 1)
            for (int i = 1; i <= 11; i++) {
                createActivePost(testUser.getId(), "ALL", "게시글" + i);
            }

            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(10);
            assertThat(response.getBody().isHasNext()).isTrue();
            assertThat(response.getBody().getNextCursor()).isNotNull();
        }

        @Test
        @DisplayName("게시글이 없으면 빈 리스트를 반환한다")
        void getPostList_empty() {
            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).isEmpty();
            assertThat(response.getBody().isHasNext()).isFalse();
        }

        @Test
        @DisplayName("작성자 정보가 포함된다")
        void getPostList_includesAuthorInfo() {
            // given
            createActivePost(testUser.getId(), "ALL", "테스트 게시글");

            // when
            ResponseEntity<PostListResponse> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    PostListResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);

            PostListResponse.PostSummary post = response.getBody().getPosts().get(0);
            assertThat(post.getAuthor().getUserId()).isEqualTo(testUser.getId());
            assertThat(post.getAuthor().getNickname()).isEqualTo("테스트유저");
            assertThat(post.getAuthor().getTeamCode()).isEqualTo("DOOSAN");
        }

        @Test
        @DisplayName("인증 없이 요청하면 401 에러")
        void getPostList_unauthorized() {
            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/community/posts",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
