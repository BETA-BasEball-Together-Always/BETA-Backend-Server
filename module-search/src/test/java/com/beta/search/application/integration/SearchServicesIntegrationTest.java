package com.beta.search.application.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.beta.core.port.PostPort;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.PostInfo;
import com.beta.docker.MysqlEsTestContainer;
import com.beta.search.application.SearchAppService;
import com.beta.search.application.SearchFacadeService;
import com.beta.search.application.dto.SearchHashtagResult;
import com.beta.search.application.dto.SearchMyLogResult;
import com.beta.search.application.dto.SearchPostResult;
import com.beta.search.application.dto.SearchSuggestionsResult;
import com.beta.search.application.dto.SearchUserResult;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.document.SearchLogDocument;
import com.beta.search.domain.document.UserDocument;
import com.beta.search.infra.repository.SearchLogRepository;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SearchServicesIntegrationTest.TestConfig.class)
class SearchServicesIntegrationTest extends MysqlEsTestContainer {

    @Autowired
    private SearchFacadeService searchFacadeService;

    @Autowired
    private SearchAppService searchAppService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SearchLogRepository searchLogRepository;

    @BeforeEach
    void set_up_search_indices() {
        recreate_index(PostDocument.class);
        recreate_index(UserDocument.class);
        recreate_index(HashtagDocument.class);
        recreate_index(SearchLogDocument.class);
    }

    @Test
    void searchWhileTyping_호출시_추천검색어와_추천유저를_반환한다() {
        // given
        index_search_log("log-1", "두산", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 0, 0));
        index_search_log("log-2", "두산베어스", 2L, "POST", LocalDateTime.of(2026, 2, 19, 12, 1, 0));
        save_documents(
                UserDocument.builder()
                        .id(1L)
                        .nickname("두산팬")
                        .bio("야구 팬")
                        .teamCode("DOOSAN")
                        .teamNameKr("두산 베어스")
                        .build(),
                UserDocument.builder()
                        .id(2L)
                        .nickname("삼성팬")
                        .bio("삼성 야구 팬")
                        .teamCode("SAMSUNG")
                        .teamNameKr("삼성 라이온즈")
                        .build()
        );

        // when
        SearchSuggestionsResult result = searchFacadeService.searchWhileTyping("두산");

        // then
        assertThat(result.suggestedKeywords()).contains("두산", "두산베어스");
        assertThat(result.suggestedUsers().stream().map(SearchSuggestionsResult.SuggestedUser::userId))
                .contains(1L);
    }

    @Test
    void searchPosts_호출시_게시글검색결과를_반환하고_검색로그를_저장한다() {
        // given
        index_post_document(
                100L,
                "ALL",
                "두산 경기 정말 좋았다",
                "유저1",
                List.of("두산", "야구"),
                "2026-02-19T12:00:00"
        );

        // when
        SearchPostResult result = searchFacadeService.searchPosts("두산", "ALL", 1L, SearchCursor.first());

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().postId()).isEqualTo(100L);
        assertThat(result.posts().getFirst().hashtags()).contains("두산", "야구");
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
        assertThat(count_search_logs(1L, "두산")).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void searchUsers_호출시_유저검색결과를_반환하고_검색로그를_저장한다() {
        // given
        save_documents(
                UserDocument.builder()
                        .id(1L)
                        .nickname("두산팬")
                        .bio("야구 팬")
                        .teamCode("DOOSAN")
                        .teamNameKr("두산 베어스")
                        .build()
        );

        // when
        SearchUserResult result = searchFacadeService.searchUsers("두산", 1L, SearchCursor.first());

        // then
        assertThat(result.users()).hasSize(1);
        assertThat(result.users().getFirst().nickname()).isEqualTo("두산팬");
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
        assertThat(count_search_logs(1L, "두산")).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void searchHashtags_호출시_해시태그검색결과를_반환하고_검색로그를_저장한다() {
        // given
        save_documents(
                HashtagDocument.builder()
                        .id(1L)
                        .tagName("두산")
                        .usageCount(10L)
                        .build()
        );

        // when
        SearchHashtagResult result = searchFacadeService.searchHashtags("두산", 1L, SearchCursor.first());

        // then
        assertThat(result.hashtags()).hasSize(1);
        assertThat(result.hashtags().getFirst().tagName()).isEqualTo("두산");
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
        assertThat(count_search_logs(1L, "두산")).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void findMyRecentKeywords_호출시_중복없이_최신검색어_최대5개를_반환한다() {
        // given
        index_search_log("l1", "두산", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 0, 0));
        index_search_log("l2", "두산", 1L, "USER", LocalDateTime.of(2026, 2, 19, 12, 1, 0));
        index_search_log("l3", "삼성", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 2, 0));
        index_search_log("l4", "한화", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 3, 0));
        index_search_log("l5", "기아", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 4, 0));
        index_search_log("l6", "롯데", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 5, 0));
        index_search_log("l7", "엘지", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 6, 0));

        // when
        SearchMyLogResult result = searchAppService.findMyRecentKeywords(1L);

        // then
        assertThat(result.logs()).hasSize(5);
        assertThat(result.logs().stream().map(SearchMyLogResult.SearchMyLogItem::keyword).distinct().count())
                .isEqualTo(result.logs().size());
    }

    @Test
    void deleteMySearchLog_호출시_본인로그만_삭제한다() {
        // given
        index_search_log("target-log", "삭제대상", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 1, 0));
        index_search_log("same-user-log", "유지대상", 1L, "POST", LocalDateTime.of(2026, 2, 19, 12, 2, 0));
        index_search_log("other-user-log", "다른유저키워드", 2L, "POST", LocalDateTime.of(2026, 2, 19, 12, 3, 0));

        String targetLogId = searchAppService.findMyRecentKeywords(1L).logs().stream()
                .filter(item -> item.keyword().equals("삭제대상"))
                .map(SearchMyLogResult.SearchMyLogItem::id)
                .findFirst()
                .orElseThrow();

        // when
        searchAppService.deleteMySearchLog(1L, targetLogId);
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();

        // then
        assertThat(searchAppService.findMyRecentKeywords(1L).logs().stream().map(SearchMyLogResult.SearchMyLogItem::keyword))
                .doesNotContain("삭제대상")
                .contains("유지대상");
        assertThat(searchAppService.findMyRecentKeywords(2L).logs().stream().map(SearchMyLogResult.SearchMyLogItem::keyword))
                .contains("다른유저키워드");
    }

    // ES에 데이터 저장
    private void save_documents(Object... documents) {
        for (Object document : documents) {
            elasticsearchOperations.save(document);
        }
        refresh_indices(); // 바로 검색 가능하도록 새로고침
    }

    // ES 인덱스 새로고침
    private void refresh_indices() {
        elasticsearchOperations.indexOps(PostDocument.class).refresh();
        elasticsearchOperations.indexOps(UserDocument.class).refresh();
        elasticsearchOperations.indexOps(HashtagDocument.class).refresh();
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
    }

    // ES 인덱스 삭제 후 재생성
    private void recreate_index(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping(); // @Document/@Field/@Setting 기준으로 재생성
    }

    private void index_search_log(String id, String keyword, Long userId, String searchType, LocalDateTime searchedAt) {
        searchLogRepository.save(
                SearchLogDocument.builder()
                        .id(id)
                        .keyword(keyword)
                        .userId(userId)
                        .searchType(searchType)
                        .searchedAt(searchedAt)
                        .build()
        );
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
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
            elasticsearchOperations.indexOps(PostDocument.class).refresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long count_search_logs(Long userId, String keyword) {
        try {
            return elasticsearchClient.count(c -> c
                    .index("search_logs")
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m.term(t -> t.field("userId").value(userId)))
                                    .must(m -> m.term(t -> t.field("keyword").value(keyword)))
                            )))
                    .count();
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
        PostPort postPort() {
            // 최소 PostInfo를 반환
            return (postIds, userId) -> postIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> PostInfo.builder()
                                    .id(id)
                                    .author(AuthorInfo.unknown(userId))
                                    .channel("ALL")
                                    .imageUrls(List.of())
                                    .hashtags(List.of("두산", "야구"))
                                    .commentCount(0)
                                    .likeCount(0)
                                    .sadCount(0)
                                    .funCount(0)
                                    .hypeCount(0)
                                    .hasLiked(false)
                                    .createdAt(LocalDateTime.of(2026, 2, 19, 12, 0, 0))
                                    .build()
                    ));
        }
    }
}
