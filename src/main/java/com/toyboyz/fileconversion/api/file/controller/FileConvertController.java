//package com.toyboyz.fileconversion.api.file.controller;
//
//import com.toyboyz.fileconversion.api.file.dto.request.FileConvertRequest;
//import com.toyboyz.fileconversion.api.file.dto.response.FileConvertResponse;
//import com.toyboyz.fileconversion.api.file.service.FileConvertService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/convert")
//public class FileConvertController {
//
//    private final FileConvertService fileConvertService;
//
//    @PostMapping
//    public ResponseEntity<FileConvertResponse> requestConvert(@RequestBody FileConvertRequest req) {
//        FileConvertResponse res = fileConvertService.requestConvert(req);
//        return ResponseEntity.ok(res);
//    }
//}
