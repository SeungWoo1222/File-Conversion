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
        SubDTO subDTO;
        try {
            subDTO = om.readValue(message,SubDTO.class);
            log.info("상태 : "+subDTO.getStatus());
            log.info(subDTO.getFilename());
            sseService.notifyRedis(subDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
