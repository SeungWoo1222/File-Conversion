package com.toyboyz.fileconversion.infra.redis.config;

import com.toyboyz.fileconversion.infra.redis.service.RedisService;
import com.toyboyz.fileconversion.infra.sse.service.StatsRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSubscribedConnectionException;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@Profile("!test")
public class RedisConfig {

    // 1. 메시지 리스너 어댑터 설정 (어떤 클래스의 어떤 메서드를 쓸지 지정)
    @Bean
    MessageListenerAdapter messageListenerAdapter(RedisService sub) {
        return new MessageListenerAdapter(sub, "subProg");
    }

    @Bean
    MessageListenerAdapter statsMessageListenerAdapter(StatsRedisSubscriber sub) {
        return new MessageListenerAdapter(sub, "onStatsUpdated");
    }


    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter,
                                            MessageListenerAdapter statsMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapter, new PatternTopic("redis-file-msg"));
        container.addMessageListener(statsMessageListenerAdapter, new PatternTopic("stats-updated"));
        return container;
    }
}