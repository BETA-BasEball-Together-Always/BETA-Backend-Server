package com.beta.application.comment;

import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.community.application.admin.AdminCommentQueryAppService;
import com.beta.community.application.admin.dto.AdminCommentQueryResult;
import com.beta.community.domain.entity.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCommentQueryFacadeService {

    private final AdminCommentQueryAppService adminCommentQueryAppService;
    private final UserReadService userReadService;

    @Transactional(readOnly = true)
    public Page<AdminCommentQueryResult> getComments(
            int page,
            int size,
            Status status,
            String keyword
    ) {
        Page<AdminCommentQueryResult> comments = adminCommentQueryAppService.getComments(page, size, status, keyword);

        Map<Long, String> authorNicknames = userReadService.findUsersByIds(
                        comments.getContent().stream()
                                .map(AdminCommentQueryResult::authorUserId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return comments.map(comment -> comment.withAuthorNickname(authorNicknames.get(comment.authorUserId())));
    }
}
