package com.toyboyz.fileconversion.infra.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyboyz.fileconversion.infra.redis.dto.SubDTO;
import com.toyboyz.fileconversion.infra.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final SseService sseService;
    private final ObjectMapper om;

    public void subProg(String message) {
        log.info("레디스 수신");
        try {
            SubDTO subDTO = om.readValue(message, SubDTO.class); //null로 들어오면 매칭 불가 -> " "
            sseService.notifyRedis(subDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
