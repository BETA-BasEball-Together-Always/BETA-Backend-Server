package com.beta.search.infra.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.beta.search.domain.document.HashtagDocument;
import com.beta.search.domain.cursor.SearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchHashtagRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<SearchHit<HashtagDocument>> searchByKeyword(String keyword, SearchCursor cursor, int size) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m.field("tagName").query(keyword))
                )
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)))
                .withMaxResults(size);

        if (cursor != null && !cursor.isFirst()) {
            queryBuilder.withSearchAfter(List.of(cursor.getScore(), cursor.getId()));
        }

        SearchHits<HashtagDocument> hits = elasticsearchOperations.search(queryBuilder.build(), HashtagDocument.class);
        return hits.getSearchHits();
    }
}
