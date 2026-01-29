package com.toyboyz.fileconversion.api.file.controller;

import com.toyboyz.fileconversion.api.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> fileUpload(@RequestParam("files")MultipartFile[] files,
                                       @RequestParam("targetFormat") String targetFormat) {

        StringBuilder sb = new StringBuilder();
        sb.append(targetFormat).append("\n");
        for (MultipartFile file : files) {
            sb.append(file.getOriginalFilename()).append("\n");
            System.out.println(file.getSize());
            sb.append(file.getSize()).append("\n");
            sb.append(file.getContentType()).append("\n");
            sb.append(file.getResource()).append("\n");
        }
        System.out.println(sb);

        return new ResponseEntity<>(HttpStatus.OK);

    }
}
