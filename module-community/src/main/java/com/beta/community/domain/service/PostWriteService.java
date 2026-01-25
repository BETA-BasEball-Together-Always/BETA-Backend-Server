package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.infra.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostWriteService {

    private final PostJpaRepository postJpaRepository;

    @Transactional
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Transactional
    public void delete(Post post) {
        postJpaRepository.delete(post);
    }

    @Transactional
    public void activate(Post post) {
        post.activate();
        postJpaRepository.save(post);
    }
}
