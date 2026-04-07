package com.beta.account.application.admin;

import com.beta.account.application.admin.dto.AdminUserStatisticsResult;
import com.beta.account.domain.entity.User;
import com.beta.account.infra.repository.AdminUserStatisticsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserStatisticsAppService {

    private final AdminUserStatisticsQueryRepository adminUserStatisticsQueryRepository;

    @Transactional(readOnly = true)
    public AdminUserStatisticsResult getUserStatistics(User.UserStatus status) {
        return AdminUserStatisticsResult.from(
                adminUserStatisticsQueryRepository.getUserStatistics(status)
        );
    }
}
