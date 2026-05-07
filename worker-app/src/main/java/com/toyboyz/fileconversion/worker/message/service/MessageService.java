package com.toyboyz.fileconversion.worker.message.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyboyz.fileconversion.infra.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.toyboyz.fileconversion.worker.conversion.service.ConversionService;
import com.toyboyz.fileconversion.infra.redis.service.RedisProgressPublisher;
import com.toyboyz.fileconversion.worker.message.dto.ParserDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

    private final ObjectMapper om;
    private final S3StorageService s3StorageService;
    private final ConversionService conversionService;
    private final RedisProgressPublisher redisProgressPublisher;

    /**
     * [as is]
     * 2~5초 이내로 db 에서 저장된 시간순으로 정렬한 뒤 1000(n)개 씩 가져온다.
     * 쿼리 오버헤드 최소화를 위한 jdbcTemplate + 쿼리를 직접 작성하여 데이터를 가져온다.
     * 이후 해당 데이터를 타입별로 분기 처리한다.
     *
     * [to be]
     * api 서버를 통해 파일 요청이 들어오면 테이블에 저장되고 로우가 저장된다.
     * 이 때 Debezium(CDC,Publisher) 가 create op 를 감지한다.
     * RabbitMQ 의 cdc.event.new_conversion 으로 메세지를 쏜다.
     * worker(consumer) 는 해당 메세지를 받아 파일을 변환한다.
     * 변환 중,변환 완료 상태의 실시간 이벤트 발생 시 레디스 pub/sub 구조로 API 에 상태를 전송한다.
     */


    //3-1 저장된 메세지를 가져온다
    //[as is] polling
    //[to be] cdc


    //03.30 현재 대기 메세지: 5000 의 큐를 소비하는 속도는 적정하나 (cpu 40% 더 올릴 수 있음)
    //s3 와의 네트워크 접근성이 너무 떨어짐 s3 (다운로드,업로드)를 기다리느라 cpu 가 놀고 있는 수준
    //이 과정에서 동시 처리수를 늘려서 메세지를 가져왔는데 ack 처리를 못받아서 unack 로 메세지가 대기되는 경우 발생
    //해결책  1.로컬망이라 aws 내부 망보다 느릴 수 있으므로 배포 후 모니터링 해볼 것 (+ S3 전용 통로(VPC Endpoint))
    //      2.메모리 버퍼 최적화
    //      3. 파일을 chunk 로 나눠서 aws sdk 의 TransferManager 를 통해 병렬 처리받아 전송한다?


    public void categorizer(String message) throws IOException {
        try {
            //메세지 내부 문자열을 파싱해서 DTO 에 담아온다.
            ParserDTO parserDTO = parseMessage(message);

            //만약 이미 pdf 파일의 경우 15 % 에서 멈추는 현상 있음, 원본 즉시 리턴해주는 에러처리 필요함

            //s3 에서 파일 다운로드
            byte[] originFile = s3StorageService.downloadFile(parserDTO.getS3Key());

            //파일 변환
            byte[] convertedFile = conversionService.imageToPdf(parserDTO,originFile);


            //클라이언트에게 반환되는 파일명으로 파싱한 뒤 변환 완료 파일 업로드
            String convertedFilename = conversionService.convertedFilename(parserDTO);
            s3StorageService.uploadFile(convertedFilename,convertedFile,parserDTO.getRequestFormat());

            //최종 진행률 100%
            redisProgressPublisher.publishProg(parserDTO.getHistoryId(), parserDTO.getUuid(),parserDTO.getFileName(),100,convertedFilename,"3",convertedFile.length);

            //서버에서 변환 완료 후 즉시 전송되면 뷰단에서 프로그래스 바보다 먼저 상태가 "변환 완료" 로 바뀔 수 있음
            //서버에서 변환 완료를 받으면 프로그래스 바를 전부 채우고 status 를 변환 완료 로 바꿔야함
            //또는 90% 대까지 올린 후 100 수신 시 즉시 변환 완료 처리
            log.info("변환 + 업로드 완료");
        } catch (Exception e) {
            log.info("Error : {}, message = {}", message, e.getMessage());
            throw e;
        }
    }




    //넘어온 payload를 ParserDTO 로 파싱한 뒤 리턴
    public ParserDTO parseMessage(String message) {
        try {
            JsonNode root = om.readTree(message);
            if (root.has("after") && root.path("after").has("payload")) {
                String payload = root.path("after").path("payload").asText();
                return om.readValue(payload, ParserDTO.class);
            }
            return om.treeToValue(root, ParserDTO.class);
        } catch (Exception e) {
            log.error("Message parsing failed. rawMessage={}", message, e);
            throw new RuntimeException("Message parsing Exception Error : " + message, e);
        }
    }
}
