package com.toyboyz.fileconversion.api.sse.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(String uuid, SseEmitter emitter) {
        emitters.put(uuid,emitter);
    }

    public SseEmitter getEmitter(String uuid) {
        return emitters.get(uuid);
    }

    public Map<String, SseEmitter> getEmitters() {
        return emitters;
    }

    public void removeEmitter(String uuid) {
        emitters.remove(uuid);
    }


    //에미터 에러 핸들링
    public void completeAndRemoveEmitter(String uuid) {
        SseEmitter emitter = emitters.get(uuid);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (IllegalStateException e) {
                emitters.remove(uuid);
            }
        }
    }

}
