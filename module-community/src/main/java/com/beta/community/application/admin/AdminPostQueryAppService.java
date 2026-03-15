package com.beta.community.application.admin;

import com.beta.community.application.admin.dto.AdminPostQueryResult;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.AdminPostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPostQueryAppService {

    private final AdminPostQueryRepository adminPostQueryRepository;

    @Transactional(readOnly = true)
    public Page<AdminPostQueryResult> getPosts(
            int page,
            int size,
            Status status,
            Post.Channel channel,
            String keyword
    ) {
        return adminPostQueryRepository.findPosts(page, size, status, channel, keyword)
                .map(AdminPostQueryResult::from);
    }
}
