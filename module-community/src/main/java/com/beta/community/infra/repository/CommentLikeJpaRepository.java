package com.beta.community.infra.repository;

import com.beta.community.domain.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLike, Long> {

    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId AND cl.commentId IN :commentIds")
    Set<Long> findLikedCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") List<Long> commentIds
    );
}
