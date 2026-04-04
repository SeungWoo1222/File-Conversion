package com.toyboyz.fileconversion.api.stats.scheduler;

import com.toyboyz.fileconversion.api.stats.service.StatsFlushService;
import com.toyboyz.fileconversion.api.stats.service.StatsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsFlushScheduler {

    private final StatsFlushService statsFlushService;

    @Scheduled(fixedDelay = 60000)
    public void flush() {
        statsFlushService.flushGlobalStatsToDb();
    }
}