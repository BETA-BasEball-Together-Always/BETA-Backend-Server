package com.beta.community.infra.repository;

import com.beta.community.domain.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLike, Long> {

    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId AND cl.commentId IN :commentIds")
    Set<Long> findLikedCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") List<Long> commentIds
    );

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.userId = :userId AND cl.commentId = :commentId")
    void deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId IN :commentIds")
    void deleteAllByCommentIdIn(@Param("commentIds") List<Long> commentIds);
}
