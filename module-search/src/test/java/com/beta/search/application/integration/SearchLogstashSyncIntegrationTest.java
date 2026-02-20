package com.beta.search.application.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.beta.core.port.PostPort;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.PostInfo;
import com.beta.docker.MysqlEsLogstashTestContainer;
import com.beta.search.application.SearchFacadeService;
import com.beta.search.application.dto.SearchHashtagResult;
import com.beta.search.application.dto.SearchPostResult;
import com.beta.search.application.dto.SearchUserResult;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.document.SearchLogDocument;
import com.beta.search.domain.document.UserDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

// test pipe line : 스케줄러 5초
@SpringBootTest(classes = SearchLogstashSyncIntegrationTest.TestConfig.class)
@Sql(scripts = "/sql/logstash-sync-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/logstash-sync-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/logstash-sync-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class SearchLogstashSyncIntegrationTest extends MysqlEsLogstashTestContainer {

    @Autowired
    private SearchFacadeService searchFacadeService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void set_up_logstash_sync_test_context() {
        recreate_index(PostDocument.class);
        recreate_index(UserDocument.class);
        recreate_index(HashtagDocument.class);
        recreate_index(SearchLogDocument.class);
    }

    @Test
    void logstash_파이프라인은_db_변경분을_posts_users_hashtags_인덱스로_동기화한다() {
        // given
        // @Sql(logstash-sync-test-data.sql)
        wait_until_documents_are_synced(100L, 10L, 20L);

        // when
        SearchPostResult postResult = searchFacadeService.searchPosts("두산", "ALL", 1L, SearchCursor.first());
        SearchUserResult userResult = searchFacadeService.searchUsers("두산", 1L, SearchCursor.first());
        SearchHashtagResult hashtagResult = searchFacadeService.searchHashtags("두산", 1L, SearchCursor.first());
        PostDocument syncedPost = get_post_document(100L);

        // then
        assertThat(postResult.posts()).isNotEmpty();
        assertThat(postResult.posts().getFirst().postId()).isEqualTo(100L);

        assertThat(userResult.users()).isNotEmpty();
        assertThat(userResult.users().getFirst().nickname()).contains("두산");

        assertThat(hashtagResult.hashtags()).isNotEmpty();
        assertThat(hashtagResult.hashtags().getFirst().tagName()).contains("두산");

        assertThat(syncedPost).isNotNull();
        assertThat(syncedPost.getAuthorNickname()).isEqualTo("두산직관러");
        assertThat(syncedPost.getHashtags()).contains("두산", "야구");
    }

    @Test
    void logstash_posts_파이프라인은_soft_delete된_게시글을_es에서_삭제한다() {
        // given
        // @Sql(logstash-sync-test-data.sql)
        wait_until_documents_are_synced(100L, 10L, 20L);

        // when
        jdbcTemplate.update(
                "UPDATE posts SET status = ?, deleted_at = NOW(6), updated_at = NOW(6) WHERE id = ?",
                "DELETED",
                100L
        );

        // then
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(document_exists("posts", "100")).isFalse());
    }

    // ES 인덱스 초기화
    private void recreate_index(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping();
    }

    // Logstash가 DB 데이터를 ES에 올릴 때까지 대기
    private void wait_until_documents_are_synced(Long postId, Long userId, Long hashtagId) {
        // 최대 40초 동안 2초 간격으로 재시도
        await().atMost(Duration.ofSeconds(40))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    // ES는 near-real-time(거의 실시간) 이므로 저장 직후엔 검색에 바로 안 보일 수 있어서 refresh 후 확인
                    refresh_if_exists(PostDocument.class);
                    refresh_if_exists(UserDocument.class);
                    refresh_if_exists(HashtagDocument.class);

                    // 3개 인덱스 문서가 모두 확인되면 대기 루프 종료
                    assertThat(document_exists("posts", String.valueOf(postId))).isTrue();
                    assertThat(document_exists("users", String.valueOf(userId))).isTrue();
                    assertThat(document_exists("hashtags", String.valueOf(hashtagId))).isTrue();
                });
    }

    // 인덱스가 존재할 때 refresh를 호출해 검색 가능 상태로 만듬
    private void refresh_if_exists(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (indexOps.exists()) {
            indexOps.refresh();
        }
    }

    // 인덱스에 문서 ID가 존재하는지 확인
    private boolean document_exists(String index, String id) {
        try {
            return elasticsearchClient.exists(e -> e.index(index).id(id)).value();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // posts 인덱스 문서를 조회해 동기화된 필드값을 검증
    private PostDocument get_post_document(Long postId) {
        try {
            GetResponse<PostDocument> response = elasticsearchClient.get(
                    g -> g.index("posts").id(String.valueOf(postId)),
                    PostDocument.class
            );
            return response.source();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(
            basePackages = "com.beta.search",
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.REGEX,
                    pattern = "com\\.beta\\.search\\.application\\.(integration|unit)\\..*"
            )
    )
    static class TestConfig {
        @Bean
        PostPort logstash_sync_test_post_port() {
            return (postIds, userId) -> postIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> PostInfo.builder()
                                    .id(id)
                                    .author(AuthorInfo.builder().userId(10L).nickname("두산직관러").teamCode("DOOSAN").build())
                                    .channel("ALL")
                                    .imageUrls(List.of())
                                    .hashtags(List.of("두산", "야구"))
                                    .commentCount(0)
                                    .likeCount(0)
                                    .sadCount(0)
                                    .funCount(0)
                                    .hypeCount(0)
                                    .hasLiked(false)
                                    .createdAt(LocalDateTime.of(2026, 2, 20, 11, 0, 0))
                                    .build()
                    ));
        }
    }
}
