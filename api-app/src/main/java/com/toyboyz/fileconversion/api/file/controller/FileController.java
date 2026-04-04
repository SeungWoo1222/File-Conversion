package com.toyboyz.fileconversion.api.file.controller;

import com.toyboyz.fileconversion.api.file.dto.request.UploadCompleteRequest;
import com.toyboyz.fileconversion.api.file.dto.request.UploadInitRequest;
import com.toyboyz.fileconversion.api.file.dto.response.UploadInitResponse;
import com.toyboyz.fileconversion.api.file.service.FileService;
import com.toyboyz.fileconversion.domain.history.entitiy.History;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload-init")
    @ResponseBody
    public UploadInitResponse uploadInit(@RequestBody UploadInitRequest req) {
        return fileService.uploadInit(req);
    }

    @PostMapping("/upload-complete")
    @ResponseBody
    public ResponseEntity<List<History>> uploadComplete(@RequestBody UploadCompleteRequest req) {
        return ResponseEntity.ok(fileService.uploadComplete(req));
    }
}
