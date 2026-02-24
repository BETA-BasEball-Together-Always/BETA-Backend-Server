package com.beta.community.infra.repository;

import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageJpaRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdAndStatusOrderBySortAsc(Long postId, Status status);

    List<PostImage> findByPostIdInAndStatusOrderByPostIdAscSortAsc(List<Long> postIds, Status status);

    @Modifying
    @Query("DELETE FROM PostImage pi WHERE pi.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
