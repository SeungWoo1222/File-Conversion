package com.toyboyz.fileconversion.api.file.service;

import com.toyboyz.fileconversion.api.file.dto.request.FileConvertRequest;
import com.toyboyz.fileconversion.api.file.dto.response.FileConvertResponse;

import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
import com.toyboyz.fileconversion.debezium.entity.OutboxEventType;
import com.toyboyz.fileconversion.debezium.entity.OutboxEvent;
import com.toyboyz.fileconversion.debezium.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileConvertService {

    private final HistoryRepository historyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public FileConvertResponse requestConvert(FileConvertRequest req) {
        // 1) History 저장 (상태 초기값 'P')
        History history = History.builder()
                .uuid(req.getUuid())
                .fileName(req.getFileName())
                .originalFile(req.getOriginalFile())
                .convertedFile(null)
                .status("P")
                .build();

        History saved = historyRepository.save(history);

        // 2) Outbox 이벤트 저장 (INSERT-only)
        String payloadJson = toJson(Map.of(
                "historyId", saved.getHistoryId(),
                "uuid", saved.getUuid(),
                "fileName", saved.getFileName(),
                "originalFile", saved.getOriginalFile(),
                "targetFormat", req.getTargetFormat()
        ));

        OutboxEvent outbox = OutboxEvent.builder()
                .aggregateType("file")
                .aggregateId(String.valueOf(saved.getHistoryId())) // historyId를 aggregate_id로 사용
                .eventType(OutboxEventType.FILE_CONVERT_REQUESTED)
                .payload(payloadJson)
                .build();

        outboxEventRepository.save(outbox);

        // 트랜잭션 커밋 후:
        // Debezium이 outbox_event INSERT를 감지 → RMQ로 publish

        return FileConvertResponse.builder()
                .historyId(saved.getHistoryId())
                .uuid(saved.getUuid())
                .status(saved.getStatus())
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 여기서 런타임 예외로 터뜨려야 트랜잭션 롤백이 되어
            // History만 저장되고 Outbox가 누락되는 불상사를 막을 수 있음
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}