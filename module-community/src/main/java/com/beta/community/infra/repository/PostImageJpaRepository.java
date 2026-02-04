package com.beta.community.infra.repository;

import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageJpaRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdAndStatusOrderBySortAsc(Long postId, Status status);
}
