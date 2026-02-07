package com.beta.search.infra.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.beta.search.domain.document.UserDocument;
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
public class SearchUserRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<SearchHit<UserDocument>> searchByKeyword(String keyword, SearchCursor cursor, int size) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s.match(m -> m.field("nickname").query(keyword).boost(2.0f)))
                                .should(s -> s.match(m -> m.field("bio").query(keyword).boost(1.0f)))
                                .minimumShouldMatch("1")
                        )
                )
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)))
                .withMaxResults(size);

        if (cursor != null && !cursor.isFirst()) {
            queryBuilder.withSearchAfter(List.of(cursor.getScore(), cursor.getId()));
        }

        SearchHits<UserDocument> hits = elasticsearchOperations.search(queryBuilder.build(), UserDocument.class);
        return hits.getSearchHits();
    }

    public List<SearchHit<UserDocument>> searchByNicknamePrefix(String keyword, SearchCursor cursor, int size) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .matchPhrasePrefix(m -> m.field("nickname").query(keyword))
                )
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("id").order(SortOrder.Asc)))
                .withMaxResults(size);

        if (cursor != null && !cursor.isFirst()) {
            queryBuilder.withSearchAfter(List.of(cursor.getScore(), cursor.getId()));
        }

        SearchHits<UserDocument> hits = elasticsearchOperations.search(queryBuilder.build(), UserDocument.class);
        return hits.getSearchHits();
    }

}
