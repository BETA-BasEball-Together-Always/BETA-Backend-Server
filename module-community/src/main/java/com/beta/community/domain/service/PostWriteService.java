package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post;
import com.beta.community.infra.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostWriteService {

    private final PostJpaRepository postJpaRepository;

    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    public void updateContent(Post post, String content) {
        post.updateContent(content);
    }

    public void softDelete(Post post) {
        post.softDelete();
    }
}
