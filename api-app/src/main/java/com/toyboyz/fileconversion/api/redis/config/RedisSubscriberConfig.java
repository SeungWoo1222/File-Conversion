package com.toyboyz.fileconversion.api.redis.config;

import com.toyboyz.fileconversion.api.redis.service.RedisMessageHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@Profile("!test")
public class RedisSubscriberConfig {

    // 1. 메시지 리스너 어댑터 설정 (어떤 클래스의 어떤 메서드를 쓸지 지정)
    @Bean
    MessageListenerAdapter messageListenerAdapter(RedisMessageHandler sub) {
        return new MessageListenerAdapter(sub, "subProg");
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            @Qualifier("messageListenerAdapter") MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapter, new PatternTopic("redis-file-msg"));
        return container;
    }
}