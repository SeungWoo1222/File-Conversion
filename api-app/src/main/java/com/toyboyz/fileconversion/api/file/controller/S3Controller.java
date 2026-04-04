package com.toyboyz.fileconversion.api.file.controller;

import com.toyboyz.fileconversion.infra.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3StorageService s3StorageService;

    @GetMapping("/api/file/download-url")
    public String downloadFile(@RequestParam String key) {
        return s3StorageService.presignGetUrl(key);
    }
}
