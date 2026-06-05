package com.toyboyz.fileconversion.api.stats.service;

import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
import com.toyboyz.fileconversion.api.history.repository.HistoryStatsProjection;
import com.toyboyz.fileconversion.api.stats.entity.ConversionStatsTotal;
import com.toyboyz.fileconversion.api.stats.repository.ConversionStatsTotalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsReconcileService {

    private static final String GLOBAL_KEY = "stats:global";
    private static final String GLOBAL_PK = "global";
    private static final String FIELD_COMPLETED_COUNT = "completedCount";
    private static final String FIELD_COMPLETED_BYTES = "completedBytes";

    // lastReconciledAt 없을 때 (최초 실행) 전체 집계 시작 기준점
    private static final LocalDateTime EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0);

    private final HistoryRepository historyRepository;
    private final ConversionStatsTotalRepository conversionStatsTotalRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void reconcileFromHistory() {
        LocalDateTime now = LocalDateTime.now();

        ConversionStatsTotal stats = conversionStatsTotalRepository.findById(GLOBAL_PK)
                .orElse(new ConversionStatsTotal(GLOBAL_PK, 0L, 0L));

        // lastReconciledAt 없으면 전체 집계 (최초 1회), 있으면 그 이후만 (델타)
        LocalDateTime from = stats.getLastReconciledAt() != null
                ? stats.getLastReconciledAt()
                : EPOCH;

        HistoryStatsProjection delta =
                historyRepository.aggregateCompletedStatsBetween(from, now);

        long deltaCount = delta == null ? 0L : delta.getCompletedCount();
        long deltaBytes = delta == null ? 0L : delta.getCompletedBytes();

        long newCount = stats.getCompletedCount() + deltaCount;
        long newBytes = stats.getCompletedBytes() + deltaBytes;

        // DB 업데이트 + 다음 실행 기준점 저장
        stats.overwriteWithTime(newCount, newBytes, now);
        conversionStatsTotalRepository.save(stats);

        // Redis 동기화
        redisTemplate.opsForHash().putAll(
                GLOBAL_KEY,
                Map.of(
                        FIELD_COMPLETED_COUNT, String.valueOf(newCount),
                        FIELD_COMPLETED_BYTES, String.valueOf(newBytes)
                )
        );

        log.info("정합성 보정 완료 (델타: {}건 / {}bytes) → 누적: {}건 / {}bytes",
                deltaCount, deltaBytes, newCount, newBytes);
    }
}