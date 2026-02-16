package com.toyboyz.fileconversion.api.history.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyboyz.fileconversion.api.file.dto.request.UploadInitRequest;
import com.toyboyz.fileconversion.api.history.dto.HistoryDTO;
import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
import com.toyboyz.fileconversion.api.sse.config.SseEmitterRegistry;
import com.toyboyz.fileconversion.api.sse.service.SseService;
import com.toyboyz.fileconversion.debezium.entity.OutboxEvent;
import com.toyboyz.fileconversion.debezium.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /** upload-init 단계: status=1, originalFile=S3 key 저장 */
    @Transactional
    public List<History> createPendingHistories(UploadInitRequest req, List<String> s3Keys) {
        if (req.files().size() != s3Keys.size()) {
            throw new IllegalArgumentException("files size != s3Keys size");
        }

        List<History> saveList = new ArrayList<>();
        for (int i = 0; i < req.files().size(); i++) {
            UploadInitRequest.FileMeta f = req.files().get(i);

            String ext = extractExtension(f.filename());

            History h = History.builder()
                    .uuid(req.uuid())
                    .fileName(f.filename())
                    .requestFormat(req.targetFormat())
                    .originalFile(s3Keys.get(i))
                    .originalFormat(ext)
                    .status("1")                 // 대기/업로드 준비
                    .build();
            saveList.add(h);
        }

        List<History> saved = historyRepository.saveAll(saveList);
        saved.forEach(eventPublisher::publishEvent);
        return saved;
    }

    /** upload-complete 단계: status=2로 바꾸고, fileConvert outbox 생성 */
    @Transactional
    public void markUploadedAndCreateConvertOutbox(List<History> histories) {
        List<OutboxEvent> outboxList = new ArrayList<>();

        for (History h : histories) {
            h.setStatus("2"); // 변환 진행 중
            eventPublisher.publishEvent(h);

            Map<String, Object> payload = new HashMap<>();
            payload.put("historyId", h.getHistoryId());
            payload.put("uuid", h.getUuid());
            payload.put("s3Key", h.getOriginalFile());
            payload.put("originalFormat", h.getOriginalFormat());
            payload.put("requestFormat", h.getRequestFormat());

            outboxList.add(OutboxEvent.of("file", h.getHistoryId(), "fileConvert", toJson(payload)));
        }

        historyRepository.saveAll(histories);
        outboxEventRepository.saveAll(outboxList);
    }

    //DTO 반환으로 리팩토링 대상
//    @Transactional
//    public List<History> saveHistory(List<MultipartFile> fileList,String format,String uuid) {
//        ArrayList<History> saveList = new ArrayList<>();
//        for (MultipartFile file : fileList) {
//            History history = History.builder()
//                    .fileName(file.getName())
//                    .originalFile(file.getOriginalFilename())
//                    .requestFormat(format)
//                    .uuid(uuid)
//                    .status("1")
//                    .build();
//            saveList.add(history);
//        }
//        List<History> hisList = historyRepository.saveAll(saveList);
//        saveList.forEach(eventPublisher::publishEvent);
//
//        // OutboxEvent 생성
//        ArrayList<OutboxEvent> outboxList = new ArrayList<>();
//        for (History history : hisList) {
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("historyId", history.getHistoryId());
//            payload.put("uuid", history.getUuid());
//            payload.put("originalFile", history.getOriginalFile());
//            payload.put("requestFormat", history.getRequestFormat());
//
//            outboxList.add(OutboxEvent.of("file", history.getHistoryId(), "fileConvert", payload.toString()));
//        }
//        outboxEventRepository.saveAll(outboxList);
//
//        return findHistories(uuid);
//    }

    @Transactional(readOnly = true)
    public List<History> findHistories(String uuid) {
        return historyRepository.findAllByUuidAndStatus(uuid,"1");
    }

    @Transactional
    public void updateStatus(Long id) {
        History history = historyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다."));
        history.setStatus("3");
        historyRepository.save(history);
        eventPublisher.publishEvent(history);
    }

    @Transactional(readOnly = true)
    public List<History> getByIds(List<Long> ids) {
        return historyRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public List<History> findAllByUuid(String uuid) {
        return historyRepository.findAllByUuid(uuid);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return null;
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return null;
        return filename.substring(idx + 1).toLowerCase();
    }
}
