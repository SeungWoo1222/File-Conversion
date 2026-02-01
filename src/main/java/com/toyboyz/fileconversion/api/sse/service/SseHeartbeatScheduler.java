package com.toyboyz.fileconversion.api.sse.service;

import com.toyboyz.fileconversion.api.sse.config.SseEmitterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class SseHeartbeatScheduler {

    public final SseEmitterRegistry sseEmitterRegistry;

    @Scheduled(fixedRate = 6000) //1분 주기
    public void sendHeartbeat() {

        Map<String, SseEmitter> emitters = sseEmitterRegistry.getEmitters();

        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            String uuid = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                log.info(uuid + " ping");
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IllegalStateException e) {
                sseEmitterRegistry.completeAndRemoveEmitter(uuid);
                log.info("SseEmitter already completed for uuid: " + uuid);
            } catch (IOException e) {
                sseEmitterRegistry.completeAndRemoveEmitter(uuid);
                log.info("Disconnected: " + uuid);
            }
        }
    }
}
