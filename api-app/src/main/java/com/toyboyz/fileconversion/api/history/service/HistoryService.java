package com.toyboyz.fileconversion.api.history.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyboyz.fileconversion.api.file.dto.request.UploadInitRequest;
import com.toyboyz.fileconversion.api.history.entitiy.History;
import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
import com.toyboyz.fileconversion.api.outbox.entity.OutboxEvent;
import com.toyboyz.fileconversion.api.outbox.repository.OutboxEventRepository;
import com.toyboyz.fileconversion.infra.redis.dto.SubDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /** upload-init 단계: status=1, originalFile=S3 key 저장 */
    @Transactional
    public List<History> createPendingHistories(UploadInitRequest req, List<String> s3Keys, List<String> s3FileNames) {
        if (req.files().size() != s3Keys.size() || req.files().size() != s3FileNames.size()) {
            throw new IllegalArgumentException("file size mismatch");
        }

        List<History> saveList = new ArrayList<>();
        for (int i = 0; i < req.files().size(); i++) {
            UploadInitRequest.FileMeta f = req.files().get(i);

            String ext = extractExtension(f.filename());

            History h = History.builder()
                    .uuid(req.uuid())
                    .fileName(f.filename())
                    .requestFormat(req.targetFormat())
                    .s3FileName(s3FileNames.get(i))
                    .originalFile(s3Keys.get(i))
                    .originalFormat(ext)
                    .status("1")                 // 대기/업로드 준비
                    .originalFileSizeBytes(f.size())
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
            // 이미 처리된 것이면 skip
            if (!"1".equals(h.getStatus())) {
                continue;
            }

            h.setStatus("2"); // 변환 진행 중
            eventPublisher.publishEvent(h);

            Map<String, Object> payload = new HashMap<>();
            payload.put("historyId", h.getHistoryId());
            payload.put("uuid", h.getUuid());
            payload.put("s3Key", h.getOriginalFile());
            payload.put("originalFormat", h.getOriginalFormat());
            payload.put("requestFormat", h.getRequestFormat());
            payload.put("fileName", h.getS3FileName());
            payload.put("originalSize", h.getOriginalFileSizeBytes());
            payload.put("originalFileName", removeExtension(h.getFileName()));

            outboxList.add(OutboxEvent.of("file", h.getHistoryId(), "fileConvert", toJson(payload)));
        }

            historyRepository.saveAll(histories);
            outboxEventRepository.saveAll(outboxList);
    }


    @Transactional
    public void updateHistoryEvent(SubDTO subDTO) {
        try {
            //변환 완료일 때만 실행
            if (subDTO.getStatus().equals("3")) {
                History history = historyRepository.findById(subDTO.getHistoryId()).orElseThrow();
                history.updateHistory(subDTO.getStatus(), subDTO.getConvertedFile());
                historyRepository.save(history);
            }
        } catch (Exception e) {
            log.info("실시간 이벤트 데이터를 통한 history 갱신 실패: {}",e.getMessage());
        }
    }






    @Transactional(readOnly = true)
    public List<History> getByIds(List<Long> ids) {
        return historyRepository.findAllById(ids);
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

    private String removeExtension(String filename) {
        if (filename == null) return null;

        int idx = filename.lastIndexOf('.');
        if (idx <= 0) return filename; // 확장자 없거나 .gitignore 같은 경우 그대로 반환

        return filename.substring(0, idx);
    }
}
