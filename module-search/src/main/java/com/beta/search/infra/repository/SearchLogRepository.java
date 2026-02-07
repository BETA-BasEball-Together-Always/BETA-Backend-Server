package com.beta.search.infra.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.json.JsonData;
import com.beta.search.domain.document.SearchLogDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchLogRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public void save(SearchLogDocument document) {
        elasticsearchOperations.save(document);
    }

    public List<String> findRecentKeywordsByUser(Long userId, int size) {

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t.field("userId").value(userId))
                )
                .withSort(s -> s
                        .field(f -> f.field("searchedAt").order(SortOrder.Desc))
                )
                .withMaxResults(size);

        SearchHits<SearchLogDocument> hits =
                elasticsearchOperations.search(queryBuilder.build(), SearchLogDocument.class);

        return hits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent().getKeyword())
                .distinct()
                .toList();
    }

    public List<String> searchByKeywordPrefix(String keyword, int size) {

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .prefix(p -> p.field("keyword").value(keyword))
                )
                .withAggregation("keywords",
                        Aggregation.of(a -> a
                                .terms(TermsAggregation.of(t -> t
                                        .field("keyword")
                                        .size(size)
                                ))
                        )
                );

        SearchHits<SearchLogDocument> hits =
                elasticsearchOperations.search(queryBuilder.build(), SearchLogDocument.class);

        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) hits.getAggregations();

        if (aggregations == null) {
            return List.of();
        }

        return aggregations
                .get("keywords")
                .aggregation()
                .getAggregate()
                .sterms()
                .buckets()
                .array()
                .stream()
                .map(StringTermsBucket::key)
                .map(FieldValue::stringValue)
                .toList();
    }

}
