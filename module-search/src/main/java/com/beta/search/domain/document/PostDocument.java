package com.beta.search.domain.document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(indexName = "posts")
@Setting(settingPath = "/elasticsearch/nori-settings.json")
public class PostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String channel; // 팀 별 필터용

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String content; // 검색 대상

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String authorNickname; // 검색 대상, 표시용

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private List<String> hashtags; // 검색 대상

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt; // 정렬용

}
