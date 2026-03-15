package com.beta.application.post;

import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.community.application.admin.AdminPostQueryAppService;
import com.beta.community.application.admin.dto.AdminPostQueryResult;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPostQueryFacadeService {

    private final AdminPostQueryAppService adminPostQueryAppService;
    private final UserReadService userReadService;

    @Transactional(readOnly = true)
    public Page<AdminPostQueryResult> getPosts(
            int page,
            int size,
            Status status,
            Post.Channel channel,
            String keyword
    ) {
        Page<AdminPostQueryResult> posts = adminPostQueryAppService.getPosts(page, size, status, channel, keyword);

        Map<Long, String> authorNicknames = userReadService.findUsersByIds(
                        posts.getContent().stream()
                                .map(AdminPostQueryResult::authorUserId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return posts.map(post -> post.withAuthorNickname(authorNicknames.get(post.authorUserId())));
    }
}
