package com.beta.controller.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.beta.SearchApiTestBase;
import com.beta.controller.search.response.SearchHashtagResponse;
import com.beta.controller.search.response.SearchMyLogsResponse;
import com.beta.controller.search.response.SearchPostResponse;
import com.beta.controller.search.response.SearchSuggestionsResponse;
import com.beta.controller.search.response.SearchUserResponse;
import com.beta.core.security.JwtTokenProvider;
import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.document.SearchLogDocument;
import com.beta.search.domain.document.UserDocument;
import com.beta.search.infra.repository.SearchLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/search-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class SearchControllerApiTest extends SearchApiTestBase {

    private static final Long TEST_USER_ID = 1L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private String accessToken;

    @BeforeEach
    void set_up() {
        accessToken = jwtTokenProvider.generateAccessToken(TEST_USER_ID, "DOOSAN", "USER");

        recreate_index(PostDocument.class);
        recreate_index(UserDocument.class);
        recreate_index(HashtagDocument.class);
        recreate_index(SearchLogDocument.class);

        seed_search_documents();
        refresh_indices();
    }

    @Test
    @DisplayName("GET /api/v1/search/suggestions")
    void get_suggestions_호출시_추천검색어와_추천유저를_반환한다() {
        // given
        // @Sql

        // when
        ResponseEntity<SearchSuggestionsResponse> response = restTemplate.exchange(
                "/api/v1/search/suggestions?keyword=두산",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchSuggestionsResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().suggestedKeywords()).contains("두산");
        assertThat(response.getBody().suggestedUsers().stream().map(SearchSuggestionsResponse.SuggestedUser::userId))
                .contains(2L);
    }

    @Test
    @DisplayName("GET /api/v1/search/posts")
    void search_posts_호출시_게시글검색결과를_반환한다() {
        // given
        // @Sql

        // when
        ResponseEntity<SearchPostResponse> response = restTemplate.exchange(
                "/api/v1/search/posts?keyword=두산&channel=ALL",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchPostResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().posts()).isNotEmpty();

        SearchPostResponse.PostItem post = response.getBody().posts().stream()
                .filter(item -> item.postId().equals(200L))
                .findFirst()
                .orElseThrow();

        assertThat(post.channel()).isEqualTo("ALL");
        assertThat(post.hashtags()).contains("두산", "야구");
        assertThat(post.author()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/search/users")
    void search_users_호출시_유저검색결과를_반환한다() {
        // given
        // @Sql

        // when
        ResponseEntity<SearchUserResponse> response = restTemplate.exchange(
                "/api/v1/search/users?keyword=두산",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchUserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().users()).isNotEmpty();
        assertThat(response.getBody().users().stream().map(SearchUserResponse.UserItem::nickname))
                .anyMatch(nickname -> nickname.contains("두산"));
    }

    @Test
    @DisplayName("GET /api/v1/search/hashtags")
    void search_hashtags_호출시_해시태그검색결과를_반환한다() {
        // given
        // @Sql

        // when
        ResponseEntity<SearchHashtagResponse> response = restTemplate.exchange(
                "/api/v1/search/hashtags?keyword=두산",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchHashtagResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().hashtags()).isNotEmpty();
        assertThat(response.getBody().hashtags().stream().map(SearchHashtagResponse.HashtagItem::tagName))
                .contains("두산");
    }

    @Test
    @DisplayName("GET /api/v1/search/my-logs")
    void get_my_logs_호출시_내_최신검색어를_반환한다() {
        // given
        // @Sql

        // when
        ResponseEntity<SearchMyLogsResponse> response = restTemplate.exchange(
                "/api/v1/search/my-logs",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchMyLogsResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().logs().stream().map(SearchMyLogsResponse.LogItem::keyword))
                .contains("삭제대상", "두산");
    }

    @Test
    @DisplayName("DELETE /api/v1/search/my-logs/{logId}")
    void delete_my_log_호출시_내_검색기록이_삭제된다() {
        // given @Sql
        ResponseEntity<SearchMyLogsResponse> beforeResponse = restTemplate.exchange(
                "/api/v1/search/my-logs",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchMyLogsResponse.class
        );

        String targetLogId = beforeResponse.getBody().logs().stream()
                .filter(log -> "삭제대상".equals(log.keyword()))
                .map(SearchMyLogsResponse.LogItem::id)
                .findFirst()
                .orElseThrow();

        // when
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/search/my-logs/" + targetLogId,
                HttpMethod.DELETE,
                new HttpEntity<>(create_auth_headers()),
                Void.class
        );
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();

        ResponseEntity<SearchMyLogsResponse> afterResponse = restTemplate.exchange(
                "/api/v1/search/my-logs",
                HttpMethod.GET,
                new HttpEntity<>(create_auth_headers()),
                SearchMyLogsResponse.class
        );

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(afterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterResponse.getBody()).isNotNull();
        assertThat(afterResponse.getBody().logs().stream().map(SearchMyLogsResponse.LogItem::keyword))
                .doesNotContain("삭제대상")
                .contains("두산");
    }

    private HttpHeaders create_auth_headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void recreate_index(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping();
    }

    private void refresh_indices() {
        elasticsearchOperations.indexOps(PostDocument.class).refresh();
        elasticsearchOperations.indexOps(UserDocument.class).refresh();
        elasticsearchOperations.indexOps(HashtagDocument.class).refresh();
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
    }

    private void seed_search_documents() {
        index_post_document(
                200L,
                "ALL",
                "오늘 두산 경기 직관 후기입니다. 타선 집중력이 정말 좋았어요.",
                "두산응원단장",
                List.of("두산", "야구"),
                "2026-02-20T11:00:00"
        );
        index_post_document(
                202L,
                "DOOSAN",
                "두산 타선이 찬스에서 집중력을 보여서 좋았습니다.",
                "두산응원단장",
                List.of("두산"),
                "2026-02-20T11:02:00"
        );

        elasticsearchOperations.save(
                UserDocument.builder()
                        .id(2L)
                        .nickname("두산응원단장")
                        .bio("두산 직관 2000번째")
                        .teamCode("DOOSAN")
                        .teamNameKr("두산 베어스")
                        .build()
        );
        elasticsearchOperations.save(
                UserDocument.builder()
                        .id(3L)
                        .nickname("엘지팬클럽")
                        .bio("엘지는 야구킹")
                        .teamCode("LG")
                        .teamNameKr("LG 트윈스")
                        .build()
        );

        elasticsearchOperations.save(
                HashtagDocument.builder()
                        .id(11L)
                        .tagName("두산")
                        .usageCount(10L)
                        .build()
        );
        elasticsearchOperations.save(
                HashtagDocument.builder()
                        .id(12L)
                        .tagName("야구")
                        .usageCount(8L)
                        .build()
        );

        searchLogRepository.save(
                SearchLogDocument.builder()
                        .id("search-log-1")
                        .keyword("두산")
                        .userId(1L)
                        .searchType("POST")
                        .searchedAt(LocalDateTime.of(2026, 2, 20, 12, 0, 0))
                        .build()
        );
        searchLogRepository.save(
                SearchLogDocument.builder()
                        .id("search-log-2")
                        .keyword("삭제대상")
                        .userId(1L)
                        .searchType("USER")
                        .searchedAt(LocalDateTime.of(2026, 2, 20, 12, 1, 0))
                        .build()
        );
        searchLogRepository.save(
                SearchLogDocument.builder()
                        .id("search-log-3")
                        .keyword("다른유저키워드")
                        .userId(2L)
                        .searchType("HASHTAG")
                        .searchedAt(LocalDateTime.of(2026, 2, 20, 12, 2, 0))
                        .build()
        );
    }

    private void index_post_document(
            Long id,
            String channel,
            String content,
            String authorNickname,
            List<String> hashtags,
            String createdAt
    ) {
        try {
            elasticsearchClient.index(i -> i
                    .index("posts")
                    .id(String.valueOf(id))
                    .document(Map.of(
                            "id", id,
                            "channel", channel,
                            "content", content,
                            "authorNickname", authorNickname,
                            "hashtags", hashtags,
                            "createdAt", createdAt
                    )));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
