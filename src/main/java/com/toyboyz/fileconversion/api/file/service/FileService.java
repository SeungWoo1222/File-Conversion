package com.toyboyz.fileconversion.api.file.service;

import com.toyboyz.fileconversion.api.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
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
    public void sendToS3(List<MultipartFile> file,String format) {
        //s3 에 파일 전송 메서드


        //파일변환 요청기록 저장
        historyService.saveHistory(file,format);
    }


}
