package com.toyboyz.fileconversion.infra.rabbit.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;


    //RabbitMQ 상수 설정
    public static final String EXCHANGE_NAME = "cdc.event.exchange";
    public static final String QUEUE_NAME = "cdc.event.new_conversion";
    public static final String ROUTING_KEY = "cdc.event.message";

    public static final String DLQ = "deadLetterQueue";
    public static final String DLX = "deadLetterExchange";

    //producer -> exchange -> queue -> routing(key) -> consumer

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME,true,false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }


    @Bean
    public Queue queue() { //큐가 종료되더라도 영속화
        return QueueBuilder.durable(QUEUE_NAME)
                //x 옵션 필요함
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key",DLQ)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ);
    }

    //다이렉트(1대1),토픽(와일드카드로 포괄적인 전송),팬아웃(여러 큐에 동일한 메세지 발송)
    @Bean
    public Binding binding(Queue queue,TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("cdc.event.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        //dlq 를 dlx 에 준다. routingKey = dlq
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    //rmq 의 연결을 관리하는 객체
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.getRabbitConnectionFactory().setRequestedHeartbeat(30);
        factory.getRabbitConnectionFactory().setConnectionTimeout(5000);
        return factory;
    }

    //tracing 할 경우 필요한 Admin 권한
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
