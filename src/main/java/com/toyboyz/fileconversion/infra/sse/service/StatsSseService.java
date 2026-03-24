package com.toyboyz.fileconversion.infra.sse.service;

import com.toyboyz.fileconversion.api.stats.dto.StatsSummaryResponse;
import com.toyboyz.fileconversion.api.stats.service.StatsQueryService;
import com.toyboyz.fileconversion.infra.sse.config.StatsSseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsSseService {

    private final StatsSseEmitterRegistry statsSseEmitterRegistry;
    private final StatsQueryService statsQueryService;

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        String emitterId = statsSseEmitterRegistry.addEmitter(emitter);

        emitter.onCompletion(() -> statsSseEmitterRegistry.removeEmitter(emitterId));
        emitter.onTimeout(() -> statsSseEmitterRegistry.removeEmitter(emitterId));
        emitter.onError(e -> statsSseEmitterRegistry.removeEmitter(emitterId));

        try {
            emitter.send(SseEmitter.event()
                    .name("stats-summary-update")
                    .data(statsQueryService.getSummary()));
        } catch (IOException e) {
            statsSseEmitterRegistry.removeEmitter(emitterId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void broadcastLatestSummary() {
        StatsSummaryResponse summary = statsQueryService.getSummary();

        List<String> deadEmitterIds = new ArrayList<>();

        for (Map.Entry<String, SseEmitter> entry : statsSseEmitterRegistry.getEmitters().entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("stats-summary-update")
                        .data(summary));
            } catch (IOException e) {
                deadEmitterIds.add(entry.getKey());
            }
        }

        for (String emitterId : deadEmitterIds) {
            statsSseEmitterRegistry.removeEmitter(emitterId);
        }
    }
}