package com.beta.scheduler;

import com.beta.account.domain.service.UserCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserCleanupService userCleanupService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredWithdrawnUsers() {
        log.info("Starting scheduled cleanup of expired withdrawn users");
        int cleanedCount = userCleanupService.cleanupExpiredWithdrawnUsers();
        log.info("Completed cleanup: {} users removed", cleanedCount);
    }
}
