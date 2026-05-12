package com.toyboyz.fileconversion.worker.message.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConsumerConfig {

    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;


    //producer -> exchange -> queue -> routing(key) -> consumer

    //RabbitMQ 상수 설정
    public static final String EXCHANGE_NAME = "cdc.event.exchange";
    public static final String QUEUE_NAME = "cdc.event.new_conversion";

    public static final String DLQ = "deadLetterQueue";
    public static final String DLX = "deadLetterExchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";


    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    //메세지 처리 실패 시 2회까지 메인 큐로 보내야함
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME,true,false);
    }

    //실패건에 대한 메세지를 처리하는 exchange
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }


    //실패 시  명확하고 빠른 처리를 위해 DLQ 는 다이렉트 방식으로 구현해보겠음
    @Bean
    public Binding deadLetterBinding() {
        //dlq 를 dlx 에 준다. routingKey = dlq
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_ROUTING_KEY);
    }


    //rmq 의 연결을 관리하는 객체
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }



    // TODO RabbitListener 설정, 수동 Ack 모드 설정
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
//        factory.setConcurrentConsumers(10);
//        factory.setMaxConcurrentConsumers(20);

        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(2);
        factory.setPrefetchCount(1);

        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 수동 Ack 모드
        return factory;
    }

}