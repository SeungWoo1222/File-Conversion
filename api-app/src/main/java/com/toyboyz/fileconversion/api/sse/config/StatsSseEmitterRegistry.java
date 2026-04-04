package com.toyboyz.fileconversion.api.sse.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatsSseEmitterRegistry {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public String addEmitter(SseEmitter emitter) {
        String emitterId = UUID.randomUUID().toString();
        emitters.put(emitterId, emitter);
        return emitterId;
    }

    public void removeEmitter(String emitterId) {
        emitters.remove(emitterId);
    }

    public Map<String, SseEmitter> getEmitters() {
        return emitters;
    }
}