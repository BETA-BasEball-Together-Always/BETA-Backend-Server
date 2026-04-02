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

    public void hide(Post post) {
        post.hide();
    }

    public void unhide(Post post) {
        post.unhide();
    }

    public void incrementCommentCount(Long postId) {
        postJpaRepository.incrementCommentCount(postId);
    }

    public void decrementCommentCount(Long postId) {
        postJpaRepository.decrementCommentCount(postId);
    }
}
