package com.toyboyz.fileconversion.infra.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyboyz.fileconversion.api.stats.service.StatsQueryService;
import com.toyboyz.fileconversion.infra.redis.dto.SubDTO;
import com.toyboyz.fileconversion.infra.sse.service.SseService;
import com.toyboyz.fileconversion.infra.sse.service.StatsSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final SseService sseService;
    private final StatsSseService statsSseService;
    private final StatsQueryService statsQueryService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper om;

    private static final String GLOBAL_KEY = "stats:global";

    public void subProg(String message) {
        log.info("레디스 수신");
        try {
            SubDTO subDTO = om.readValue(message, SubDTO.class); //null로 들어오면 매칭 불가 -> " "
            sseService.notifyRedis(subDTO);

            // 완료 상태면 전역 통계 Redis 값 증가 및 SSE 전송
            if (isCompleted(subDTO)) {
                statsQueryService.ensureGlobalStatsKey();
                redisTemplate.opsForHash().increment(GLOBAL_KEY, "completedCount", 1);
                redisTemplate.opsForHash().increment(GLOBAL_KEY, "completedBytes", subDTO.getSize());
                statsSseService.broadcastLatestSummary();
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCompleted(SubDTO subDTO) {
        return subDTO.getPercent() == 100
                && "3".equals(subDTO.getStatus());
    }
}
