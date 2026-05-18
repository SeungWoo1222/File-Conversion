package com.toyboyz.fileconversion.api.stats.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.toyboyz.fileconversion.api.stats.dto.StatsSummaryResponse;
import com.toyboyz.fileconversion.api.stats.entity.ConversionStatsTotal;
import com.toyboyz.fileconversion.api.stats.repository.ConversionStatsTotalRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsQueryService {

    private static final String GLOBAL_KEY = "stats:global";
    private static final String GLOBAL_PK = "global";
    private static final String COMPLETED_COUNT = "completedCount";
    private static final String COMPLETED_BYTES = "completedBytes";

    private final StringRedisTemplate redisTemplate;
    private final ConversionStatsTotalRepository conversionStatsTotalRepository;

    /**
     * 서버 시작 시 1회 실행.
     * 부팅 직후 Redis에 통계 값을 미리 채워둔다.
     *
     * 이렇게 해두면 첫 요청들이 동시에 들어와도 모두 Redis에서 바로 읽어가므로
     * 동시 다발 DB 조회(Thundering Herd)로 풀이 고갈되는 현상을 막을 수 있다.
     */
    private final ReentrantLock dbFallbackLock = new ReentrantLock();

    private final Cache<String, StatsSummaryResponse> fallbackCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(60, TimeUnit.SECONDS)
                    .maximumSize(1)
                    .build();

    @PostConstruct
    public void warmUpStatsCache() {
        try {
            ensureGlobalStatsKey();
            log.info("[StatsQueryService] 시작 시 Redis 통계 캐시 워밍업 완료");
        } catch (Exception e) {
            // Redis가 부팅 시점에 아직 안 떠있을 수 있으므로 실패해도 부팅은 막지 않음.
            // 첫 getSummary() 호출 시 ensureGlobalStatsKey()가 다시 시도함.
            log.warn("[StatsQueryService] 시작 시 워밍업 실패 (Redis 미가용 가능): {}", e.getMessage());
        }
    }

    public StatsSummaryResponse getSummary() {
        try {
            ensureGlobalStatsKey();
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(GLOBAL_KEY);
            return StatsSummaryResponse.builder()
                    .completedCount(toLong(entries.get(COMPLETED_COUNT)))
                    .completedBytes(toLong(entries.get(COMPLETED_BYTES)))
                    .realTime(true)
                    .build();
        } catch (Exception e) {
            log.warn("[StatsQueryService] Redis 조회 실패, fallback 진행: {}", e.getMessage());
            return getFromCacheOrDb();
        }
    }

    private StatsSummaryResponse getFromCacheOrDb() {
        StatsSummaryResponse cached = fallbackCache.getIfPresent(GLOBAL_PK);
        if (cached != null) return cached;

        if (dbFallbackLock.tryLock()) {
            try {
                cached = fallbackCache.getIfPresent(GLOBAL_PK);
                if (cached != null) return cached;

                try {
                    ConversionStatsTotal stats = conversionStatsTotalRepository
                            .findById(GLOBAL_PK)
                            .orElse(new ConversionStatsTotal(GLOBAL_PK, 0L, 0L));
                    StatsSummaryResponse response = StatsSummaryResponse.builder()
                            .completedCount(stats.getCompletedCount())
                            .completedBytes(stats.getCompletedBytes())
                            .realTime(false)
                            .notice("현재 실시간 통계 서비스에 일시적인 문제가 발생하여 마지막으로 기록된 통계를 표시합니다.")
                            .build();
                    fallbackCache.put(GLOBAL_PK, response);
                    return response;
                } catch (Exception dbException) {
                    log.error("[StatsQueryService] DB 조회도 실패: {}", dbException.getMessage());
                    return StatsSummaryResponse.builder()
                            .completedCount(0L)
                            .completedBytes(0L)
                            .realTime(false)
                            .notice("현재 통계 서비스에 문제가 발생하였습니다. 잠시 후 정상화됩니다.")
                            .build();
                }
            } finally {
                dbFallbackLock.unlock();
            }
        }

        cached = fallbackCache.getIfPresent(GLOBAL_PK);
        if (cached != null) return cached;

        return StatsSummaryResponse.builder()
                .completedCount(0L)
                .completedBytes(0L)
                .realTime(false)
                .notice("현재 통계 서비스에 문제가 발생하였습니다. 잠시 후 정상화됩니다.")
                .build();
    }

    public void ensureGlobalStatsKey() {
        Boolean hasKey = redisTemplate.hasKey(GLOBAL_KEY);
        if (!Boolean.TRUE.equals(hasKey)) {
            ConversionStatsTotal stats = conversionStatsTotalRepository.findById(GLOBAL_PK)
                    .orElse(new ConversionStatsTotal(GLOBAL_PK, 0L, 0L));
            redisTemplate.opsForHash().put(GLOBAL_KEY, COMPLETED_COUNT, String.valueOf(stats.getCompletedCount()));
            redisTemplate.opsForHash().put(GLOBAL_KEY, COMPLETED_BYTES, String.valueOf(stats.getCompletedBytes()));
        }
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }
}
