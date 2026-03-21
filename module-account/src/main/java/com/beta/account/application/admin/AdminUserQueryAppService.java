package com.beta.account.application.admin;

import com.beta.account.application.admin.dto.AdminUserQueryResult;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserQueryAppService {

    private final UserQueryRepository userQueryRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserQueryResult> getUsers(
            int page,
            int size,
            User.UserStatus status,
            String keyword
    ) {
        return userQueryRepository.findUsersExcludingAdmins(page, size, status, keyword)
                .map(AdminUserQueryResult::from);
    }
}
