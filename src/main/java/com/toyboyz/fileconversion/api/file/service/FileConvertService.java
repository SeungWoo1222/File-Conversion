//package com.toyboyz.fileconversion.api.file.service;
//
//import com.toyboyz.fileconversion.api.file.dto.request.FileConvertRequest;
//import com.toyboyz.fileconversion.api.file.dto.response.FileConvertResponse;
//
//import com.toyboyz.fileconversion.api.history.entity.History;
//import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
//import com.toyboyz.fileconversion.debezium.entity.OutboxEventType;
//import com.toyboyz.fileconversion.debezium.entity.OutboxEvent;
//import com.toyboyz.fileconversion.debezium.repository.OutboxEventRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class FileConvertService {
//
//    private final HistoryRepository historyRepository;
//    private final OutboxEventRepository outboxEventRepository;
//
//    @Transactional
//    public FileConvertResponse requestConvert(FileConvertRequest req) {
//        // 1) History 저장 (상태 초기값 'P')
//        History history = History.builder()
//                .uuid(req.getUuid())
//                .fileName(req.getFileName())
//                .originalFile(req.getOriginalFile())
//                .convertedFile(null)
//                .status("P")
//                .build();
//
//
//
//        // 트랜잭션 커밋 후:
//        // Debezium이 outbox_event INSERT를 감지 → RMQ로 publish
//
//        return FileConvertResponse.builder()
//                .historyId(saved.getHistoryId())
//                .uuid(saved.getUuid())
//                .status(saved.getStatus())
//                .build();
//    }
//
//
//}