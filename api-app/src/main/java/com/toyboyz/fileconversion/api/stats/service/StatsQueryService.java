package com.toyboyz.fileconversion.api.stats.service;

import com.toyboyz.fileconversion.api.stats.dto.StatsSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsQueryService {

    private static final String GLOBAL_KEY = "stats:global";

    private final StringRedisTemplate redisTemplate;
    private static final String COMPLETED_COUNT = "completedCount";
    private static final String COMPLETED_BYTES = "completedBytes";

    public StatsSummaryResponse getSummary() {
        ensureGlobalStatsKey();

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(GLOBAL_KEY);

        return StatsSummaryResponse.builder()
                .completedCount(toLong(entries.get(COMPLETED_COUNT)))
                .completedBytes(toLong(entries.get(COMPLETED_BYTES)))
                .build();
    }

//    private final ConversionStatsTotalRepository conversionStatsTotalRepository;

//    public StatsSummaryResponse getSummary() {
//        Boolean hasKey = redisTemplate.hasKey(GLOBAL_KEY);
//
//        if (Boolean.TRUE.equals(hasKey)) {
//            Map<Object, Object> entries = redisTemplate.opsForHash().entries(GLOBAL_KEY);
//
//            return StatsSummaryResponse.builder()
//                    .completedCount(toLong(entries.get("completedCount")))
//                    .completedBytes(toLong(entries.get("completedBytes")))
//                    .build();
//        }
//
//        ConversionStatsTotal stats = conversionStatsTotalRepository.findById("global")
//                .orElse(new ConversionStatsTotal("global", 0L, 0L));
//
//        return StatsSummaryResponse.builder()
//                .completedCount(stats.getCompletedCount())
//                .completedBytes(stats.getCompletedBytes())
//                .build();
//    }

    public void ensureGlobalStatsKey() {
        redisTemplate.opsForHash().putIfAbsent(GLOBAL_KEY, COMPLETED_COUNT, "0");
        redisTemplate.opsForHash().putIfAbsent(GLOBAL_KEY, COMPLETED_BYTES, "0");
    }


    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }
}