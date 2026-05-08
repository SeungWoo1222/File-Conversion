package com.toyboyz.fileconversion.index.controller;

import com.toyboyz.fileconversion.api.file.dto.request.UploadMetricsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/file")
public class UploadMetricsController {

    @PostMapping("/upload-speed-log")
    public ResponseEntity<Void> receiveMetrics(@RequestBody UploadMetricsRequest request) {

        log.info("=== [S3 클라이언트 업로드 속도 측정 결과] ===");
        log.info("Session UUID : {}", request.getUuid());

        if (request.getMetrics() != null && !request.getMetrics().isEmpty()) {
            for (UploadMetricsRequest.FileMetric metric : request.getMetrics()) {
                log.info("▶ HistoryId: [{}] | 크기: {} Bytes | 소요시간: {} ms | 속도: {} Mbps",
                        metric.getHistoryId(),
                        metric.getFileSize(),
                        metric.getDurationMs(),
                        metric.getSpeedMbps());
            }
        } else {
            log.warn("▶ 수신된 측정 데이터가 없습니다.");
        }
        log.info("===============================================");

        return ResponseEntity.ok().build();
    }
}
