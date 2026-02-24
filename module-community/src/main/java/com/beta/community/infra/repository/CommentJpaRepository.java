package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.depth = 0 AND c.id < :cursor ORDER BY c.createdAt DESC")
    List<Comment> findParentComments(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IN :parentIds ORDER BY c.createdAt DESC")
    List<Comment> findRepliesByParentIds(
            @Param("postId") Long postId,
            @Param("parentIds") List<Long> parentIds
    );

    @Query("SELECT DISTINCT c.parentId FROM Comment c WHERE c.parentId IN :parentIds AND c.status = :status")
    List<Long> findParentIdsWithActiveReplies(
            @Param("parentIds") List<Long> parentIds,
            @Param("status") Status status
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    void decrementLikeCount(@Param("commentId") Long commentId);

    @Query("SELECT DISTINCT c.postId FROM Comment c WHERE c.userId = :userId AND c.status = 'ACTIVE' ORDER BY c.postId DESC")
    List<Long> findDistinctPostIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c.id FROM Comment c WHERE c.postId IN :postIds")
    List<Long> findAllIdsByPostIdIn(@Param("postIds") List<Long> postIds);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId IN :postIds")
    void deleteAllByPostIdIn(@Param("postIds") List<Long> postIds);
}
