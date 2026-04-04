package com.toyboyz.fileconversion.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import com.toyboyz.fileconversion.infra.redis.dto.EventDTO;

@Service
@RequiredArgsConstructor
public class RedisProgressPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;

    //변환중인 파일의 진행 상태를 담은 message 를 api 서버로 보냄
    //worker pub -> api sub
    public void publishProg(Long historyId, String uuid, String filename, int percent,String convertedFile, String status,int size) {
        redisTemplate.convertAndSend(topic.getTopic(),buildEvent(historyId,uuid,filename,percent,convertedFile,status,size));
    }


    private EventDTO buildEvent(Long historyId, String uuid, String filename, int percent,String convertedFile, String status,int size) {
        return EventDTO.builder()
                .historyId(historyId)
                .uuid(uuid)
                .fileName(filename)
                .percent(percent)
                .convertedFile(convertedFile)
                .status(status)
                .size(size)
                .build();
    }
}
