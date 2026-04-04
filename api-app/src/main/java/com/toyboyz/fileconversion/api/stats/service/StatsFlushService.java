package com.toyboyz.fileconversion.api.stats.service;

import com.toyboyz.fileconversion.api.stats.entity.ConversionStatsTotal;
import com.toyboyz.fileconversion.api.stats.repository.ConversionStatsTotalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsFlushService {

    private static final String GLOBAL_KEY = "stats:global";
    private static final String GLOBAL_PK = "global";

    private final StringRedisTemplate redisTemplate;
    private final ConversionStatsTotalRepository conversionStatsTotalRepository;

    @Transactional
    public void flushGlobalStatsToDb() {
        Boolean hasKey = redisTemplate.hasKey(GLOBAL_KEY);

        if (!Boolean.TRUE.equals(hasKey)) {
            log.info("Redis key '{}' 가 없어서 flush를 건너뜁니다.", GLOBAL_KEY);
            return;
        }

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(GLOBAL_KEY);

        long completedCount = toLong(entries.get("completedCount"));
        long completedBytes = toLong(entries.get("completedBytes"));

        ConversionStatsTotal stats = conversionStatsTotalRepository.findById(GLOBAL_PK)
                .orElse(new ConversionStatsTotal(GLOBAL_PK, 0L, 0L));

        stats.overwrite(completedCount, completedBytes);
        conversionStatsTotalRepository.save(stats);

        log.info("글로벌 통계 flush 완료 - completedCount: {}, completedBytes: {}",
                completedCount, completedBytes);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }
}