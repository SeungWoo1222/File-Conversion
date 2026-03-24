package com.toyboyz.fileconversion.infra.sse.service;

import com.toyboyz.fileconversion.infra.sse.service.StatsSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsRedisSubscriber {

    private final StatsSseService statsSseService;

    public void onStatsUpdated(String message) {
        log.info("전역 통계 업데이트 메시지 수신: {}", message);
        statsSseService.broadcastLatestSummary();
    }
}