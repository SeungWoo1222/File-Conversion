package com.toyboyz.fileconversion.api.file.service;

import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final HistoryService historyService;

    //히스토리 저장
    public List<History> sendToS3(List<MultipartFile> file,
                                  String format,
                                  String uuid) {
        //s3 에 파일 전송 메서드

        //파일변환 요청기록 저장
        return historyService.saveHistory(file,format,uuid);
    }
}
