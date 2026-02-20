package com.beta.search.infra.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.beta.core.exception.search.SearchFailedException;
import com.beta.search.domain.document.PostDocument;
import com.beta.search.domain.cursor.SearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchPostRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<Hit<PostDocument>> searchInChannel(String keyword, String channel, SearchCursor cursor, int size) {
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index("posts")
                .query(q -> q
                        .bool(b -> b
                                .filter(f -> f.term(t -> t.field("channel").value(channel)))
                                .should(s -> s.match(m -> m.field("content").query(keyword).boost(3.0f)))
                                .should(s -> s.match(m -> m.field("hashtags").query(keyword).boost(2.0f)))
                                .should(s -> s.match(m -> m.field("authorNickname").query(keyword).boost(1.0f)))
                                .minimumShouldMatch("1")
                        )
                )
                .highlight(h -> h
                        .fields("content", f -> f
                                .preTags("<em>")
                                .postTags("</em>")
                                .fragmentSize(50)
                                .numberOfFragments(1)
                        )
                )
                .sort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .sort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)))
                .size(size);

        if (cursor != null && !cursor.isFirst()) {
            requestBuilder.searchAfter(
                    FieldValue.of(cursor.getScore()),
                    FieldValue.of(cursor.getId())
            );
        }

        try {
            SearchResponse<PostDocument> response = elasticsearchClient.search(requestBuilder.build(), PostDocument.class);
            return response.hits().hits();
        } catch (IOException e) {
            throw new SearchFailedException(e);
        }
    }
}
