package com.beta.search.infra.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.core.exception.search.SearchFailedException;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.cursor.SearchCursor;
import com.beta.search.domain.sort.SearchPostSort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
    @RequiredArgsConstructor
public class SearchPostRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<Hit<PostDocument>> searchInChannel(String keyword, String channel, SearchCursor cursor, int size) {
        return searchInChannel(keyword, channel, SearchPostSort.RECOMMENDED, cursor, size);
    }

    public List<Hit<PostDocument>> searchInChannel(
            String keyword,
            String channel,
            SearchPostSort sort,
            SearchCursor cursor,
            int size
    ) {
        Query baseQuery = Query.of(q -> q
                .bool(b -> b
                        .filter(f -> f.term(t -> t.field("channel").value(channel)))
                        .should(s -> s.match(m -> m.field("content").query(keyword).boost(3.0f)))
                        .should(s -> s.match(m -> m.field("hashtags").query(keyword).boost(2.0f)))
                        .should(s -> s.match(m -> m.field("authorNickname").query(keyword).boost(1.0f)))
                        .minimumShouldMatch("1")
                )
        );

        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index("posts")
                .highlight(h -> h
                        .fields("content", f -> f
                                .preTags("<em>")
                                .postTags("</em>")
                                .fragmentSize(50)
                                .numberOfFragments(1)
                        )
                )
                .size(size);

        applySort(sort, requestBuilder, baseQuery);

        if (cursor != null && !cursor.isFirst()) {
            applySearchAfter(sort, requestBuilder, cursor);
        }

        try {
            SearchResponse<PostDocument> response = elasticsearchClient.search(requestBuilder.build(), PostDocument.class);
            return response.hits().hits();
        } catch (IOException e) {
            throw new SearchFailedException(e);
        }
    }

    private void applySort(SearchPostSort sort, SearchRequest.Builder requestBuilder, Query baseQuery) {
        switch (sort) {
            case LATEST -> requestBuilder
                    .query(baseQuery)
                    .sort(s -> s.field(f -> f.field("createdAt").format("epoch_millis").order(SortOrder.Desc)))
                    .sort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));
            case POPULAR -> requestBuilder
                    .query(baseQuery)
                    .sort(s -> s.field(f -> f.field("popularityScore").order(SortOrder.Desc).missing(0)))
                    .sort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));
            case RECOMMENDED -> requestBuilder
                    .query(q -> q
                            .functionScore(fs -> fs
                                    .query(baseQuery)
                                    .functions(f -> f
                                            .gauss(g -> g
                                                    .date(d -> d
                                                            .field("createdAt")
                                                            .placement(p -> p
                                                                    .origin("now")
                                                                    .scale(Time.of(t -> t.time("14d")))
                                                                    .offset(Time.of(t -> t.time("1d")))
                                                                    .decay(0.5)
                                                            )
                                                    )
                                            )
                                            .weight(4.0)
                                    )
                                    .scoreMode(FunctionScoreMode.Sum)
                                    .boostMode(FunctionBoostMode.Sum)
                            )
                    )
                    .sort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                    .sort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)));
        }
    }

    private void applySearchAfter(SearchPostSort sort, SearchRequest.Builder requestBuilder, SearchCursor cursor) {
        switch (sort) {
            case LATEST, POPULAR -> requestBuilder.searchAfter(
                    FieldValue.of(cursor.getScore().longValue()),
                    FieldValue.of(cursor.getId())
            );
            case RECOMMENDED -> requestBuilder.searchAfter(
                    FieldValue.of(cursor.getScore()),
                    FieldValue.of(cursor.getId())
            );
        }
    }
}
