package com.toyboyz.fileconversion.infra.redis.config;

import com.toyboyz.fileconversion.infra.redis.service.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSubscribedConnectionException;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    // 1. 메시지 리스너 어댑터 설정 (어떤 클래스의 어떤 메서드를 쓸지 지정)
    @Bean
    MessageListenerAdapter messageListenerAdapter(RedisService sub) {
        return new MessageListenerAdapter(sub, "subProg");
    }


    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapter, new PatternTopic("redis-file-msg"));
        return container;
    }
}