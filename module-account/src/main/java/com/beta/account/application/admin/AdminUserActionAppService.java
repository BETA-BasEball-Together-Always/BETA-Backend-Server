package com.beta.account.application.admin;

import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.account.domain.service.UserStatusService;
import com.beta.account.domain.service.UserWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserActionAppService {

    private final UserReadService userReadService;
    private final UserStatusService userStatusService;
    private final UserWriteService userWriteService;

    @Transactional
    public void suspendUser(Long targetUserId) {
        User user = userReadService.findUserById(targetUserId);
        userStatusService.validateSuspend(user);
        userWriteService.suspend(user);
    }

    @Transactional
    public void unsuspendUser(Long targetUserId) {
        User user = userReadService.findUserById(targetUserId);
        userStatusService.validateUnsuspend(user);
        userWriteService.unsuspend(user);
    }
}
