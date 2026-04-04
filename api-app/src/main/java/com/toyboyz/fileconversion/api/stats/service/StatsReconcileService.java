package com.toyboyz.fileconversion.api.stats.service;

import com.toyboyz.fileconversion.history.repository.HistoryRepository;
import com.toyboyz.fileconversion.history.repository.HistoryStatsProjection;
import com.toyboyz.fileconversion.stats.entity.ConversionStatsTotal;
import com.toyboyz.fileconversion.stats.repository.ConversionStatsTotalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsReconcileService {

    private static final String GLOBAL_KEY = "stats:global";
    private static final String GLOBAL_PK = "global";
    private static final String FIELD_COMPLETED_COUNT = "completedCount";
    private static final String FIELD_COMPLETED_BYTES = "completedBytes";

    private final HistoryRepository historyRepository;
    private final ConversionStatsTotalRepository conversionStatsTotalRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void reconcileFromHistory() {
        HistoryStatsProjection result = historyRepository.aggregateCompletedStats();

        long completedCount = result == null ? 0L : result.getCompletedCount();
        long completedBytes = result == null ? 0L : result.getCompletedBytes();

        ConversionStatsTotal stats = conversionStatsTotalRepository.findById(GLOBAL_PK)
                .orElse(new ConversionStatsTotal(GLOBAL_PK, 0L, 0L));

        stats.overwrite(completedCount, completedBytes);
        conversionStatsTotalRepository.save(stats);

        redisTemplate.opsForHash().putAll(
                GLOBAL_KEY,
                Map.of(
                        FIELD_COMPLETED_COUNT, String.valueOf(completedCount),
                        FIELD_COMPLETED_BYTES, String.valueOf(completedBytes)
                )
        );

        log.info("history 기준 정합성 보정 완료 - completedCount: {}, completedBytes: {}",
                completedCount, completedBytes);
    }
}