package com.toyboyz.fileconversion.infra.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisService {

    public void subProg(String message) {
        log.info("message : {} ",message);
    }
}
