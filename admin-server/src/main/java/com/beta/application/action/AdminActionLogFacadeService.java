package com.beta.application.action;

import com.beta.account.domain.entity.User;
import com.beta.account.domain.service.UserReadService;
import com.beta.application.action.dto.AdminActionLogResult;
import com.beta.domain.entity.AdminLogAction;
import com.beta.domain.entity.AdminLog;
import com.beta.infra.repository.AdminLogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminActionLogFacadeService {

    private final AdminLogQueryRepository adminLogQueryRepository;
    private final UserReadService userReadService;

    @Transactional(readOnly = true)
    public Page<AdminActionLogResult> getActionLogs(
            int page,
            int size,
            AdminLogAction action,
            LocalDate from,
            LocalDate to
    ) {
        Page<AdminLog> actionLogs = adminLogQueryRepository.findActionLogs(page, size, action, from, to);
        List<Long> actorAdminIds = actionLogs.getContent().stream()
                .map(AdminLog::getActorAdminId)
                .distinct()
                .toList();

        List<User> admins = userReadService.findUsersByIds(actorAdminIds);
        Map<Long, String> adminNicknames = admins.stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return actionLogs.map(actionLog -> AdminActionLogResult.from(
                actionLog,
                adminNicknames.get(actionLog.getActorAdminId())
        ));
    }
}
