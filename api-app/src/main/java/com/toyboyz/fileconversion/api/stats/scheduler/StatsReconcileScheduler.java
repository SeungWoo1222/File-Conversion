package com.toyboyz.fileconversion.api.stats.scheduler;

import com.toyboyz.fileconversion.api.stats.service.StatsReconcileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsReconcileScheduler {

    private final StatsReconcileService statsReconcileService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void reconcileAtMidnight() {
        statsReconcileService.reconcileFromHistory();
    }
}