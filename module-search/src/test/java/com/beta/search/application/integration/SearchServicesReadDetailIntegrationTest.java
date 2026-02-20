package com.beta.search.application.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.core.port.PostPort;
import com.beta.core.port.dto.AuthorInfo;
import com.beta.core.port.dto.PostInfo;
import com.beta.docker.MysqlEsTestContainer;
import com.beta.search.application.SearchAppService;
import com.beta.search.application.SearchFacadeService;
import com.beta.search.application.dto.SearchHashtagResult;
import com.beta.search.application.dto.SearchPostResult;
import com.beta.search.application.dto.SearchSuggestionsResult;
import com.beta.search.application.dto.SearchUserResult;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.document.SearchLogDocument;
import com.beta.search.domain.document.UserDocument;
import com.beta.search.infra.repository.SearchPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SearchServicesReadDetailIntegrationTest.TestConfig.class)
class SearchServicesReadDetailIntegrationTest extends MysqlEsTestContainer {

    private static final Logger log = LoggerFactory.getLogger(SearchServicesReadDetailIntegrationTest.class);

    @Autowired
    private SearchFacadeService searchFacadeService;

    @Autowired
    private SearchAppService searchAppService;

    @Autowired
    private SearchPostRepository searchPostRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @BeforeEach
    void set_up_search_indices() {
        recreate_index(PostDocument.class);
        recreate_index(UserDocument.class);
        recreate_index(HashtagDocument.class);
        recreate_index(SearchLogDocument.class);
    }

    @Test
    void search_while_typing_호출시_prefix_기준_추천검색어와_닉네임_추천유저를_반환한다() {
        // given
        save_documents(
                SearchLogDocument.builder().id("l1").keyword("두산").userId(1L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 0)).build(),
                SearchLogDocument.builder().id("l2").keyword("두산").userId(1L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 1)).build(),
                SearchLogDocument.builder().id("l3").keyword("두산").userId(2L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 2)).build(),
                SearchLogDocument.builder().id("l4").keyword("두산베어스").userId(1L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 3)).build(),
                SearchLogDocument.builder().id("l5").keyword("두산베어스").userId(2L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 4)).build(),
                SearchLogDocument.builder().id("l6").keyword("두산팬").userId(1L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 5)).build(),
                SearchLogDocument.builder().id("l7").keyword("엘지").userId(1L).searchType("POST").searchedAt(LocalDateTime.of(2026, 2, 20, 10, 6)).build(),
                UserDocument.builder().id(1L).nickname("두산응원단").bio("야구 팬").teamCode("DOOSAN").teamNameKr("두산 베어스").build(),
                UserDocument.builder().id(2L).nickname("두산베어스사랑").bio("KBO 팬").teamCode("DOOSAN").teamNameKr("두산 베어스").build(),
                UserDocument.builder().id(3L).nickname("랜덤유저").bio("두산 사랑").teamCode("LG").teamNameKr("LG 트윈스").build()
        );

        // when
        SearchSuggestionsResult result = searchFacadeService.searchWhileTyping("두산");

        // then
        assertThat(result.suggestedKeywords()).containsExactly("두산", "두산베어스", "두산팬");
        assertThat(result.suggestedUsers().stream().map(SearchSuggestionsResult.SuggestedUser::userId))
                .contains(1L, 2L)
                .doesNotContain(3L); // bio 는 조건에 포함 안됨
    }

    @Test
    void search_posts_호출시_content_hashtag_author_가중치순으로_정렬된다() {
        // given
        index_post_document(1L, "ALL", "두산이 오늘 승리했다", "작성자A", List.of("야구"), "2026-02-20T11:00:00");
        index_post_document(2L, "ALL", "오늘 경기 리뷰", "작성자B", List.of("두산"), "2026-02-20T11:01:00");
        index_post_document(3L, "ALL", "오늘 경기 리뷰", "두산작성자", List.of("야구"), "2026-02-20T11:02:00");

        // when
        SearchPostResult result = searchFacadeService.searchPosts("두산", "ALL", 1L, SearchCursor.first());

        // then
        assertThat(result.posts().stream().map(SearchPostResult.SearchPostItem::postId).toList())
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void search_posts_호출시_content_매칭결과는_snippet에_하이라이트를_포함한다() {
        // given
        index_post_document(10L, "ALL", "오늘 두산이 이김", "작성자A", List.of("야구"), "2026-02-20T11:10:00");

        // when
        SearchPostResult result = searchFacadeService.searchPosts("두산", "ALL", 1L, SearchCursor.first());

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().snippet())
                .contains("<em>")
                .contains("</em>")
                .contains("두산");
    }

    @Test
    void search_posts_호출시_긴_한글_content의_snippet_반환값을_로그로_확인한다() {
        // given
        String longContent = "오늘 경기 총평을 길게 적어봅니다. 선발 투수가 초반에 흔들렸지만 수비 집중력이 살아났고, "
                + "중반 이후 타선이 연결되면서 분위기를 가져왔습니다. 특히 두산 중심 타선이 찬스에서 집중력을 보였고 "
                + "불펜도 위기 상황을 잘 막아냈습니다. 8회 수비에서 나온 병살 플레이가 흐름을 완전히 가져왔고 "
                + "마지막 공격에서도 두산 타자들이 끈질기게 승부해 추가점을 만들었습니다. 전체적으로 투타 밸런스가 좋았습니다.";
        index_post_document(11L, "ALL", longContent, "작성자", List.of("야구", "리뷰"), "2026-02-20T11:11:00");

        // when
        SearchPostResult result = searchFacadeService.searchPosts("경기", "ALL", 1L, SearchCursor.first());
        String snippet = result.posts().getFirst().snippet();

        // then
        log.info("원문 길이: {}", longContent.length());
        log.info("스니펫 길이: {}", snippet != null ? snippet.length() : 0);
        log.info("스니펫 내용: {}", snippet);

        assertThat(result.posts()).hasSize(1);
        assertThat(snippet).isNotBlank();
        assertThat(snippet).contains("<em>").contains("</em>");
        assertThat(snippet.length()).isLessThan(longContent.length());
    }

    @Test
    void search_users_호출시_nickname_bio_가중치순으로_정렬된다() {
        // given
        save_documents(
                UserDocument.builder().id(1L).nickname("두산팬").bio("야구팬").teamCode("DOOSAN").teamNameKr("두산 베어스").build(),
                UserDocument.builder().id(2L).nickname("랜덤유저").bio("두산 팬").teamCode("LG").teamNameKr("LG 트윈스").build()
        );

        // when
        SearchUserResult result = searchFacadeService.searchUsers("두산", 1L, SearchCursor.first());

        // then
        assertThat(result.users()).isNotEmpty();
        assertThat(result.users().getFirst().userId()).isEqualTo(1L);
    }

    @Test
    void search_posts_호출시_검색결과가_11건_이상이면_첫_페이지는_10건이고_has_next는_true다() {
        // given
        for (long id = 1; id <= 12; id++) {
            index_post_document(id, "ALL", "커서 토큰", "작성자", List.of("커서"), "2026-02-20T12:00:00");
        }

        // when
        SearchPostResult firstPage = searchFacadeService.searchPosts("커서", "ALL", 1L, SearchCursor.first());

        // then
        assertThat(firstPage.posts()).hasSize(10);
        assertThat(firstPage.hasNext()).isTrue();
    }

    @Test
    void search_posts_호출시_커서로_다음_페이지를_조회하면_중복없이_이어진다() {
        // given
        for (long id = 1; id <= 12; id++) {
            index_post_document(id, "ALL", "커서 토큰", "작성자", List.of("커서"), "2026-02-20T12:00:00");
        }

        // when
        SearchPostResult firstPage = searchFacadeService.searchPosts("커서", "ALL", 1L, SearchCursor.first());
        List<Hit<PostDocument>> firstPageHitsForCursor =
                searchPostRepository.searchInChannel("커서", "ALL", SearchCursor.first(), 10);
        Hit<PostDocument> lastHit = firstPageHitsForCursor.getLast();
        SearchCursor nextCursor = SearchCursor.of(lastHit.score().floatValue(), lastHit.source().getId());
        SearchPostResult secondPage = searchFacadeService.searchPosts("커서", "ALL", 1L, nextCursor);

        // then
        assertThat(firstPage.posts()).hasSize(10);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.posts()).hasSize(2);
        assertThat(secondPage.hasNext()).isFalse();

        List<Long> firstIds = firstPage.posts().stream().map(SearchPostResult.SearchPostItem::postId).toList();
        List<Long> secondIds = secondPage.posts().stream().map(SearchPostResult.SearchPostItem::postId).toList();

        assertThat(secondIds).doesNotContainAnyElementsOf(firstIds);
    }

    @Test
    void search_hashtags_호출시_검색결과가_10건_이하면_has_next는_false다() {
        // given
        List<HashtagDocument> hashtags =
                java.util.stream.LongStream.rangeClosed(1, 8)
                        .mapToObj(id -> HashtagDocument.builder()
                                .id(id)
                                .tagName("커서")
                                .usageCount(id)
                                .build())
                        .toList();
        save_documents(hashtags.toArray());

        // when
        SearchHashtagResult firstPage = searchFacadeService.searchHashtags("커서", 1L, SearchCursor.first());

        // then
        assertThat(firstPage.hashtags()).hasSize(8);
        assertThat(firstPage.hasNext()).isFalse();
    }

    private void save_documents(Object... documents) {
        for (Object document : documents) {
            elasticsearchOperations.save(document);
        }
        refresh_indices();
    }

    private void refresh_indices() {
        elasticsearchOperations.indexOps(PostDocument.class).refresh();
        elasticsearchOperations.indexOps(UserDocument.class).refresh();
        elasticsearchOperations.indexOps(HashtagDocument.class).refresh();
        elasticsearchOperations.indexOps(SearchLogDocument.class).refresh();
    }

    private void recreate_index(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping();
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
        @Primary
        PostPort search_read_test_post_port() {
            return (postIds, userId) -> postIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> PostInfo.builder()
                                    .id(id)
                                    .author(AuthorInfo.builder().userId(userId).nickname("searcher").teamCode("ALL").build())
                                    .channel("ALL")
                                    .imageUrls(List.of())
                                    .hashtags(List.of("두산", "커서"))
                                    .commentCount(0)
                                    .likeCount(0)
                                    .sadCount(0)
                                    .funCount(0)
                                    .hypeCount(0)
                                    .hasLiked(false)
                                    .createdAt(LocalDateTime.of(2026, 2, 20, 12, 0))
                                    .build()
                    ));
        }
    }
}
