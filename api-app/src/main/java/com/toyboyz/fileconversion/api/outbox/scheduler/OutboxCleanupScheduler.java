package com.toyboyz.fileconversion.api.outbox.scheduler;

import com.toyboyz.fileconversion.api.outbox.entity.OutboxEvent;
import com.toyboyz.fileconversion.api.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxCleanupScheduler {

    private static final int CHUNK_SIZE = 1000;
    private static final int RETENTION_DAYS = 7;

    private final OutboxEventRepository outboxEventRepository;

    // reconcileAtMidnight(00:00)과 부하 분산을 위해 새벽 3시로 분리
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cleanOldOutboxEvents() {
        LocalDateTime before = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int totalDeleted = 0;

        while (true) {
            List<OutboxEvent> batch = outboxEventRepository.findOldEvents(before, CHUNK_SIZE);
            if (batch.isEmpty()) break;

            outboxEventRepository.deleteAll(batch);
            totalDeleted += batch.size();

            // 청크 간 짧은 대기로 DB 부하 분산
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("[OutboxCleanup] {}일 이전 이벤트 삭제 완료 - 총 {}건", RETENTION_DAYS, totalDeleted);
    }
}
