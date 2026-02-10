package com.toyboyz.fileconversion.api.history.service;

import com.toyboyz.fileconversion.api.history.dto.HistoryDTO;
import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
import com.toyboyz.fileconversion.api.sse.config.SseEmitterRegistry;
import com.toyboyz.fileconversion.api.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;

    //DTO 반환으로 리팩토링 대상
    @Transactional
    public List<History> saveHistory(List<MultipartFile> fileList,String format,String uuid) {
        ArrayList<History> saveList = new ArrayList<>();
        for (MultipartFile file : fileList) {
            History history = History.builder()
                    .fileName(file.getName())
                    .originalFile(file.getOriginalFilename())
                    .requestFormat(format)
                    .uuid(uuid)
                    .status("1")
                    .build();
            saveList.add(history);
        }
        historyRepository.saveAll(saveList);
        saveList.forEach(eventPublisher::publishEvent);
        return findHistories(uuid);
    }



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
}
