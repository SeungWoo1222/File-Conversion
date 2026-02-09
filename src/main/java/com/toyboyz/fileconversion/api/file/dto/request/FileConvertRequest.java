package com.toyboyz.fileconversion.api.file.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileConvertRequest {
    private String uuid;         // 클라이언트/서버가 쓰는 요청 추적용
    private String fileName;     // 파일명
    private String originalFile; // S3 object key 또는 URL
    private String targetFormat; // 예: "png", "pdf"
}