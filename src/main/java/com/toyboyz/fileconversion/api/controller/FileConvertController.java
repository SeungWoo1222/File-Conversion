package com.toyboyz.fileconversion.api.controller;

import com.toyboyz.fileconversion.api.dto.request.FileConvertRequest;
import com.toyboyz.fileconversion.api.dto.response.FileConvertResponse;
import com.toyboyz.fileconversion.api.service.FileConvertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/convert")
public class FileConvertController {

    private final FileConvertService fileConvertService;

    @PostMapping
    public ResponseEntity<FileConvertResponse> requestConvert(@RequestBody FileConvertRequest req) {
        FileConvertResponse res = fileConvertService.requestConvert(req);
        return ResponseEntity.ok(res);
    }
}
