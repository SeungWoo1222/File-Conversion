package com.toyboyz.fileconversion.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitInfraConfig {

    public static final String OUTBOX_EXCHANGE = "outbox.exchange";
    public static final String OUTBOX_QUEUE = "outbox.queue";

    // Debezium Outbox SMT가 만드는 routing key prefix에 맞춤
    public static final String ROUTING_PATTERN = "outbox.event.#";

    @Bean
    public TopicExchange outboxExchange() {
        return ExchangeBuilder.topicExchange(OUTBOX_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue outboxQueue() {
        return QueueBuilder.durable(OUTBOX_QUEUE).build();
    }

    @Bean
    public Binding outboxBinding(Queue outboxQueue, TopicExchange outboxExchange) {
        return BindingBuilder.bind(outboxQueue)
                .to(outboxExchange)
                .with(ROUTING_PATTERN);
    }
}

