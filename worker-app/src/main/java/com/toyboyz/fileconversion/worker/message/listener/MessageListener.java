package com.toyboyz.fileconversion.worker.message.listener;


import com.rabbitmq.client.Channel;
import com.toyboyz.fileconversion.worker.message.config.RabbitMQConsumerConfig;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import com.toyboyz.fileconversion.worker.message.service.MessageService;


import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);
    private final MessageService dataService;
    private final RabbitTemplate rabbitTemplate;
    private final RetryTemplate retryTemplate;
    private final MessageService messageService;


    //1.큐 연결
    //tag 객체가 메세지가 가진 고유번호를 가짐
    @RabbitListener(queues = RabbitMQConsumerConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void consume(String message,@Header(AmqpHeaders.DELIVERY_TAG) long tag,Channel channel) throws IOException {
        try {

            //keepAlive 생략
            //Keep Alive 는 연결된 모든 컨슈머 받아야하므로 팬아웃으로 하는게 좋을 것 같다.
            if (message.startsWith("Connection")) {
                log.info("{}",message);
                channel.basicAck(tag, false);
                return;
            }

            retryTemplate.execute(context -> {
            messageService.categorizer(message);
                return null;
            });

            channel.basicAck(tag,false);
            log.info("메세지 최종 처리 성공 : {}", message);
        } catch (Exception e) {
            //RetryTemplate 에 의해 3회 시도 후에는 내부적으로 nack 처리 -> DLQ 로 보냄(RabbitMQConfig 에서 지정)
            channel.basicNack(tag,false,false);
            log.info("3회 시도 모두 실패. DLQ 이관 : {}", e.getMessage());
        }
    }
}