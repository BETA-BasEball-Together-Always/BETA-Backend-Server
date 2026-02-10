package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.depth = 0 AND c.id > :cursor ORDER BY c.createdAt ASC")
    List<Comment> findParentComments(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IN :parentIds ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentIds(
            @Param("postId") Long postId,
            @Param("parentIds") List<Long> parentIds
    );

    @Query("SELECT DISTINCT c.parentId FROM Comment c WHERE c.parentId IN :parentIds AND c.status = :status")
    List<Long> findParentIdsWithActiveReplies(
            @Param("parentIds") List<Long> parentIds,
            @Param("status") Status status
    );
}
