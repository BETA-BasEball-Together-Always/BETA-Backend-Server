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

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(indexName = "users")
@Setting(settingPath = "/elasticsearch/nori-settings.json")
public class UserDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String nickname; // 검색 대상

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String bio; // 검색 대상

    @Field(type = FieldType.Keyword)
    private String teamCode;

    @Field(type = FieldType.Keyword)
    private String teamNameKr;

}
