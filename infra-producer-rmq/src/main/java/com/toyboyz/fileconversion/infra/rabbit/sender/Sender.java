
package com.toyboyz.fileconversion.infra.rabbit.sender;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.toyboyz.fileconversion.infra.rabbit.config.RabbitMQConfig;

import java.time.LocalDateTime;
import java.util.Properties;

@Component
@RequiredArgsConstructor
@EnableScheduling //#### error
public class Sender {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    //기본 전송 메서드
    public void send(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME,message);
    }


    public void testSend(String message) {
        message = "{\"before\":null,\"after\":{\"aggregate_id\":20559,\"created_at\":1774829501421020,\"id\":151,\"updated_at\":1774829501421020,\"aggregate_type\":\"file\",\"event_type\":\"fileConvert\",\"payload\":\"{\\\"originalFileName\\\":\\\"download-1\\\",\\\"fileName\\\":\\\"40baeef8-48c1-4e73-99a1-d215b0635f19\\\",\\\"s3Key\\\":\\\"uploads/ec0924a7-5607-4bd8-b099-8396574970a9/40baeef8-48c1-4e73-99a1-d215b0635f19\\\",\\\"historyId\\\":20559,\\\"requestFormat\\\":\\\"PDF\\\",\\\"originalFormat\\\":\\\"png\\\",\\\"originalSize\\\":5745,\\\"uuid\\\":\\\"ec0924a7-5607-4bd8-b099-8396574970a9\\\"}\"},\"source\":{\"version\":\"3.4.1.Final\",\"connector\":\"mysql\",\"name\":\"cdc.event\",\"ts_ms\":1774797101000,\"snapshot\":\"false\",\"db\":\"api\",\"sequence\":null,\"ts_us\":1774797101000000,\"ts_ns\":1774797101000000000,\"table\":\"outbox_event\",\"server_id\":355390513,\"gtid\":null,\"file\":\"mysql-bin-changelog.011788\",\"pos\":888,\"row\":0,\"thread\":3892,\"query\":null},\"transaction\":null,\"op\":\"c\",\"ts_ms\":1774797101466,\"ts_us\":1774797101466450,\"ts_ns\":1774797101466450134}";
        for (int i = 0; i < 1000; i++) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                    "cdc.event.#"
                    ,message);
        }
    }



    @Scheduled(fixedRate = 30000)
    private void sendKeepAlive() {
        //커넥션된 컨슈머가 있을 때만 보냄
        Properties properties = rabbitAdmin.getQueueProperties(RabbitMQConfig.QUEUE_NAME);
        if (properties != null) {
            int consumerCnt = (Integer) properties.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);
            if (consumerCnt > 0) {
                String heartbeatMsg = "Connection_Keep_Alive : (" + LocalDateTime.now() + ")";
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        heartbeatMsg);
            }
        }
    }
}
