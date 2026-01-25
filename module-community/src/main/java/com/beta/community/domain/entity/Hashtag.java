package com.beta.community.domain.entity;

import com.beta.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table( name = "hashtag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag extends BaseEntity {

    @Column(name = "tag_name", nullable = false, unique = true, length = 20)
    private String tagName;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    @Builder
    public Hashtag(String tagName) {
        this.tagName = tagName;
        this.usageCount = 0L;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }
}
