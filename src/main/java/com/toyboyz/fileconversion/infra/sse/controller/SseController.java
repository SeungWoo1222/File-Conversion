package com.toyboyz.fileconversion.infra.sse.controller;

import com.toyboyz.fileconversion.api.history.service.HistoryService;
import com.toyboyz.fileconversion.infra.sse.config.SseEmitterRegistry;
import com.toyboyz.fileconversion.infra.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final HistoryService historyService;



    @GetMapping(value = "/api/sse/connect/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String uuid) {
        // 1. Emitter 생성 (타임아웃 60분 설정 예시)
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        // 2. Registry에 저장 (그래야 나중에 서비스에서 찾아서 쏠 수 있음)
        sseEmitterRegistry.addEmitter(uuid, emitter);

        // 3. 연결 직후 더미 데이터 전송 (503 에러 방지 및 연결 확인)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            sseEmitterRegistry.removeEmitter(uuid);
        }

        // 4. 연결 종료/타임아웃 시 처리
        emitter.onCompletion(() -> sseEmitterRegistry.removeEmitter(uuid));
        emitter.onTimeout(() -> sseEmitterRegistry.removeEmitter(uuid));

        return emitter;
    }


    @PatchMapping("/{id}")
    public void patchStatus(@PathVariable("id") Long id) {
        historyService.updateStatus(id);
    }

}
