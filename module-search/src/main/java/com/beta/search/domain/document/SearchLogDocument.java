package com.beta.search.domain.document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(indexName = "search_logs")
public class SearchLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String keyword;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String searchType;  // POST, USER, HASHTAG

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime searchedAt; // 검색 시각, 인기 검색어 기간 필터용

    public static SearchLogDocument create(String keyword, Long userId, String searchType) {
        return SearchLogDocument.builder()
                .keyword(keyword)
                .userId(userId)
                .searchType(searchType)
                .searchedAt(LocalDateTime.now())
                .build();
    }

}
