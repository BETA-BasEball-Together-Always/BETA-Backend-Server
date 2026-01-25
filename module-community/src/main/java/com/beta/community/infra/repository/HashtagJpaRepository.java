package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagJpaRepository extends JpaRepository<Hashtag, Long> {

    List<Hashtag> findByTagNameIn(List<String> tagNames);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO hashtag (tag_name, usage_count, created_at, updated_at)
            VALUES (:tagName, 0, NOW(), NOW())
            """, nativeQuery = true)
    void insertIgnore(@Param("tagName") String tagName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Hashtag h SET h.usageCount = h.usageCount + 1 WHERE h.tagName IN :tagNames")
    void incrementUsageCountByTagNames(@Param("tagNames") List<String> tagNames);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Hashtag h SET h.usageCount = CASE WHEN h.usageCount > 0 THEN h.usageCount - 1 ELSE 0 END WHERE h.tagName IN :tagNames")
    void decrementUsageCountByTagNames(@Param("tagNames") List<String> tagNames);
}
