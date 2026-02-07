package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostHashtagJpaRepository extends JpaRepository<PostHashtag, Long> {
    List<PostHashtag> findByPostId(Long postId);

    @Query("SELECT ph FROM PostHashtag ph JOIN FETCH ph.hashtag WHERE ph.post.id IN :postIds")
    List<PostHashtag> findByPost_IdIn(@Param("postIds") List<Long> postIds);

    void deleteAllByPostIdAndHashtagIn(Long postId, List<Hashtag> hashtags);
}
