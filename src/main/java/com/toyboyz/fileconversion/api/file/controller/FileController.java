package com.toyboyz.fileconversion.api.file.controller;

import com.toyboyz.fileconversion.api.file.service.FileService;
import com.toyboyz.fileconversion.api.history.entity.History;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    //파일 변환 요청 시 해당 컨트롤러 호출
    //파일을 s3로 전송 + 기록 저장
    //Debezium 이 기록을 캡처해서 큐로 보내야함
    @PostMapping("/upload")
    public ResponseEntity<?> fileUpload(@RequestParam("files") List<MultipartFile> files,
                                        @RequestParam("targetFormat") String targetFormat,
                                        @RequestParam("uuid") String uuid) {
        List<History> res = fileService.sendToS3(files,targetFormat,uuid);
        return new ResponseEntity<>(res,HttpStatus.OK);

    }
}
