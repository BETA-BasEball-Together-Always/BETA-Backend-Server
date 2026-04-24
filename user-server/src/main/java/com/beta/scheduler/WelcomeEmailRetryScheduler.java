package com.beta.scheduler;

import com.beta.account.domain.service.WelcomeEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailRetryScheduler {

    private final WelcomeEmailService welcomeEmailService;

    @Scheduled(fixedDelay = 60000)
    public void retryFailedWelcomeEmails() {
        int retryTargetCount = welcomeEmailService.retryFailedWelcomeEmails();
        if (retryTargetCount > 0) {
            log.info("Completed scheduled retry for failed welcome emails: count={}", retryTargetCount);
        }
    }
}
