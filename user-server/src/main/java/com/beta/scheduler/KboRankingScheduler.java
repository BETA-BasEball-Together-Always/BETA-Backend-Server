package com.beta.scheduler;

import com.beta.core.infra.client.kbo.KboRankingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KboRankingScheduler {

    private final KboRankingClient kboRankingClient;

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshKboRankingAtMidnight() {
        log.info("Starting scheduled KBO ranking refresh at midnight");
        kboRankingClient.refreshCache();
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void refreshKboRankingAtSixAM() {
        log.info("Starting scheduled KBO ranking refresh at 6 AM");
        kboRankingClient.refreshCache();
    }
}
