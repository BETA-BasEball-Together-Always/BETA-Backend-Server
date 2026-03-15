package com.beta.community.application.admin;

import com.beta.community.application.admin.dto.AdminCommentQueryResult;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.AdminCommentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCommentQueryAppService {

    private final AdminCommentQueryRepository adminCommentQueryRepository;

    @Transactional(readOnly = true)
    public Page<AdminCommentQueryResult> getComments(
            int page,
            int size,
            Status status,
            String keyword
    ) {
        return adminCommentQueryRepository.findComments(page, size, status, keyword)
                .map(AdminCommentQueryResult::from);
    }
}
