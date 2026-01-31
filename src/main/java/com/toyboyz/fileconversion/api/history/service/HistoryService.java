package com.toyboyz.fileconversion.api.history.service;

import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    public void saveHistory(List<MultipartFile> fileList,String format) {
        ArrayList<History> saveList = new ArrayList<>();
        for (MultipartFile file : fileList) {
            History history = History.builder()
                    .fileName(file.getName())
                    .originalFile(file.getOriginalFilename())
                    .requestFormat(format)
                    .build();
            saveList.add(history);
        }
        historyRepository.saveAll(saveList);
    }
}
