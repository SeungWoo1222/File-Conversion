package com.toyboyz.fileconversion.api.file.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UploadMetricsRequest {

    private String uuid;
    private List<FileMetric> metrics;

    @Data
    public static class FileMetric {
        private Long historyId;
        private Long fileSize;    // 바이트 단위
        private Long durationMs;  // 밀리초 단위
        private String speedMbps; // Mbps 단위 (문자열 형태)
    }
}