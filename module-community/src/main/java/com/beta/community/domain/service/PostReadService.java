package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.core.exception.community.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostReadService {

    private final PostJpaRepository postJpaRepository;

    public Post findById(Long postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
    }

    public Post findActiveById(Long postId) {
        Post post = findById(postId);
        if (!post.isActive()) {
            throw new PostNotFoundException();
        }
        return post;
    }
}
