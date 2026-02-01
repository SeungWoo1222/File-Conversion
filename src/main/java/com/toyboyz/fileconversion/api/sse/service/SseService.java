package com.toyboyz.fileconversion.api.sse.service;

import com.toyboyz.fileconversion.api.history.dto.HistoryDTO;
import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.sse.config.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final SseEmitterRegistry sseEmitterRegistry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHistoryEvent(History history) {
        System.out.println("이벤트 리스너 ");
        notify(history);
    }


    //id 별 emitter 보관
    public void notify(History history) {
        String uuid = history.getUuid();
        SseEmitter emitter = sseEmitterRegistry.getEmitter(uuid);

        if (emitter == null) {
            log.warn("SSE 연결이 존재하지 않습니다. 사용자 ID : {}", uuid);
            return;
        }
        try {
            String statusMsg = getStatusMessage(history.getStatus());
            emitter.send(SseEmitter.event()
                    .name("history-update")
                    .data(Map.of(
                    "id", history.getHistoryId(),
                            "status",history.getStatus(),
                            "message", statusMsg
                            )));
            log.info("Notify 메서드 실행됨! ID: {}, Status: {}", history.getHistoryId(), history.getStatus());
        } catch (IOException e) {
            log.info("SSE 연결이 끊어졌습니다. 사용자 ID : {}",uuid);
            emitter.completeWithError(e);
            sseEmitterRegistry.removeEmitter(uuid);
        }
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "1" -> "기록 생성 완료";
            case "2" -> "변환 중...";
            case "3" -> "변환 완료";
            default -> "상태 업데이트";
        };
    }
}

