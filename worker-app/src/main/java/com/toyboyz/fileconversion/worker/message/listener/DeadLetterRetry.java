package com.toyboyz.fileconversion.worker.message.listener;


import com.rabbitmq.client.Channel;
import com.toyboyz.fileconversion.common.slack.SlackNotifier;
import com.toyboyz.fileconversion.worker.message.config.RabbitMQConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.toyboyz.fileconversion.infra.redis.service.RedisProgressPublisher;
import com.toyboyz.fileconversion.worker.message.dto.ParserDTO;
import com.toyboyz.fileconversion.worker.message.service.MessageService;


@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterRetry {

    private final RabbitTemplate rabbitTemplate;
    private final SlackNotifier slackNotifier;
    private final RedisProgressPublisher redisProgressPublisher;
    private final MessageService messageService;

    //DLQ 동작 클래스
    //만약 메세지 수신에 실패하는 경우 3번까지 시도하고 최종 실패 시(basicNack)
    //DLQ 로 전송
    @RabbitListener(queues = RabbitMQConsumerConfig.DLQ,containerFactory = "rabbitListenerContainerFactory")
    public void processDeadLetter(String message, @Header(AmqpHeaders.DELIVERY_TAG) long tag, Channel channel) {
        //DLQ 의 용도 : log 수집 + DLQ 로 진입 시 redis 로 변화 실패를 API 서버로 넘겨야함
        try {
            ParserDTO parserDTO = messageService.parseMessage(message);
            //API 서버로 상태 전송 + slack 로그 전송하고 메세지 소진(ack)
            redisProgressPublisher.publishProg(parserDTO.getHistoryId(),parserDTO.getUuid(), parserDTO.getFileName(),0,null,"9",0);
            slackNotifier.sendSlackNotification(slackNotifier.createErrorMessage(message));
            channel.basicAck(tag,false);
        } catch (Exception e) {
            log.info("[DLQ] Failed to reprocess message: {} ", e.getMessage());
        }
    }

}
