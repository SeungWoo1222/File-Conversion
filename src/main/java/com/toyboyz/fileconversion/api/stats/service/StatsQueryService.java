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

    public StatsSummaryResponse getSummary() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(GLOBAL_KEY);

        return StatsSummaryResponse.builder()
                .completedCount(toLong(entries.get("completedCount")))
                .completedBytes(toLong(entries.get("completedBytes")))
                .build();
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }
}