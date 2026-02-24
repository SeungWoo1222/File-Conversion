package com.toyboyz.fileconversion.api.file.service;

import com.toyboyz.fileconversion.api.file.dto.request.UploadCompleteRequest;
import com.toyboyz.fileconversion.api.file.dto.request.UploadInitRequest;
import com.toyboyz.fileconversion.api.file.dto.response.UploadInitResponse;
import com.toyboyz.fileconversion.api.history.entity.History;
import com.toyboyz.fileconversion.api.history.service.HistoryService;
import com.toyboyz.fileconversion.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final HistoryService historyService;
    private final S3StorageService s3StorageService;

    @Value("${app.s3.prefix:uploads}")
    private String prefix;

    private static final Pattern SAFE_EXT = Pattern.compile("^[a-z0-9]{1,10}$");

    /** 1) upload-init: history 생성 + presigned url 반환 */
    public UploadInitResponse uploadInit(UploadInitRequest req) {
        // 1) 서버가 key를 먼저 결정 (클라가 임의 key 제출 못하게)
        List<String> s3Keys = req.files().stream()
                .map(f -> buildS3Key(req.uuid(), f.filename()))
                .toList();

        // 2) DB에 History 먼저 생성(원본 key 저장)
        List<History> histories = historyService.createPendingHistories(req, s3Keys);

        // 3) presigned url 발급해서 내려줌 (historyId/key/url 매핑)
        List<UploadInitResponse.Item> items = IntStream.range(0, histories.size())
                .mapToObj(i -> {
                    History h = histories.get(i);
                    UploadInitRequest.FileMeta meta = req.files().get(i);

                    String contentType = (meta.contentType() == null || meta.contentType().isBlank())
                            ? "application/octet-stream"
                            : meta.contentType();

                    String url = s3StorageService.presignPutUrl(h.getOriginalFile(), contentType);
                    return new UploadInitResponse.Item(h.getHistoryId(), h.getOriginalFile(), url);
                })
                .toList();

        return new UploadInitResponse(items);
    }

    /** 2) upload-complete: S3 업로드 존재 확인 + 상태 업데이트 + outbox 생성 */
    public List<History> uploadComplete(UploadCompleteRequest req) {
        List<History> histories = historyService.getByIds(req.historyIds());

        // 1) uuid 검증 + S3 존재 확인
        for (History h : histories) {
            if (!Objects.equals(h.getUuid(), req.uuid())) {
                throw new IllegalArgumentException("uuid가 일치하지 않는 history가 포함되어 있습니다. historyId=" + h.getHistoryId());
            }
            if (!s3StorageService.exists(h.getOriginalFile())) {
                throw new IllegalStateException("S3 업로드가 확인되지 않았습니다. key=" + h.getOriginalFile());
            }
        }

        // 2) DB 상태 변경 + outbox 생성은 HistoryService에게 위임
        historyService.markUploadedAndCreateConvertOutbox(histories);
        return historyService.findAllByUuid(req.uuid());
    }

    private String buildS3Key(String uuid, String filename) {
        String ext = extractSafeExtension(filename); // ".pdf" or ""
        return prefix + "/" + uuid + "/" + UUID.randomUUID() + ext;
    }

    /**
     * filename에서 확장자만 추출해서 ".ext" 형태로 반환.
     * - 확장자가 영문/숫자만 아니면(한글, 공백, 특수문자 등) 확장자 제거
     */
    private String extractSafeExtension(String filename) {
        if (filename == null || filename.isBlank()) return "";

        // 혹시 경로가 들어오는 경우 대비해서 마지막 파일명만 사용
        String base = filename;
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        if (slash >= 0) base = base.substring(slash + 1);

        int dot = base.lastIndexOf('.');
        if (dot <= 0 || dot == base.length() - 1) return ""; // 확장자 없음 or ".hidden" or 끝이 점

        String ext = base.substring(dot + 1).toLowerCase(Locale.ROOT);

        // 안전한 확장자만 허용 (영문/숫자)
        if (!SAFE_EXT.matcher(ext).matches()) return "";

        return "." + ext;
    }
}
