package com.beta.community.infra.repository;

import com.beta.community.domain.entity.Hashtag;
import com.beta.community.domain.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostHashtagJpaRepository extends JpaRepository<PostHashtag, Long> {
    List<PostHashtag> findByPostId(Long postId);

    List<PostHashtag> findByPost_IdIn(List<Long> postIds);

    void deleteAllByPostIdAndHashtagIn(Long postId, List<Hashtag> hashtags);
}
