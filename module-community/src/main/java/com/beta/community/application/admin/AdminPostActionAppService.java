package com.beta.community.application.admin;

import com.beta.community.domain.entity.Post;
import com.beta.community.domain.service.PostReadService;
import com.beta.community.domain.service.PostStatusService;
import com.beta.community.domain.service.PostWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPostActionAppService {

    private final PostReadService postReadService;
    private final PostStatusService postStatusService;
    private final PostWriteService postWriteService;

    @Transactional
    public void hidePost(Long targetPostId) {
        Post post = postReadService.findById(targetPostId);
        postStatusService.validateHide(post);
        postWriteService.hide(post);
    }

    @Transactional
    public void unhidePost(Long targetPostId) {
        Post post = postReadService.findById(targetPostId);
        postStatusService.validateUnhide(post);
        postWriteService.unhide(post);
    }
}
