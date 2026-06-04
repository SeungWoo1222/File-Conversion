package com.toyboyz.fileconversion.api.file.service;

import com.toyboyz.fileconversion.api.file.dto.request.UploadCompleteRequest;
import com.toyboyz.fileconversion.api.file.dto.request.UploadInitRequest;
import com.toyboyz.fileconversion.api.file.dto.response.UploadInitResponse;
import com.toyboyz.fileconversion.api.global.exception.FileUploadNotConfirmedException;
import com.toyboyz.fileconversion.api.global.exception.InvalidFileException;
import com.toyboyz.fileconversion.api.global.exception.UnauthorizedAccessException;
import com.toyboyz.fileconversion.api.history.entitiy.History;
import com.toyboyz.fileconversion.api.history.service.HistoryService;
import com.toyboyz.fileconversion.infra.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final HistoryService historyService;
    private final S3StorageService s3StorageService;

    @Value("${s3.prefix:uploads}")
    private String prefix;

    private static final Pattern SAFE_EXT = Pattern.compile("^[a-z0-9]{1,10}$");

    /** 1) upload-init: history 생성 + presigned url 반환 */
    public UploadInitResponse uploadInit(UploadInitRequest req) {
        // 파일 개수만큼 "S3에 쓸 UUID"를 먼저 만든다
        List<String> s3FileNames = req.files().stream()
                .map(f -> UUID.randomUUID().toString())
                .toList();

        // 1) 서버가 key를 먼저 결정 (클라가 임의 key 제출 못하게)
        List<String> s3Keys = s3FileNames.stream()
                .map(id -> buildS3Key(req.uuid(), id))
                .toList();

        // 2) DB에 History 먼저 생성(원본 key 저장)
        List<History> histories = historyService.createPendingHistories(req, s3Keys, s3FileNames);

        // 3) presigned url 발급해서 내려줌 (historyId/key/url 매핑)
        Map<String, History> byKey = histories.stream()
                .collect(java.util.stream.Collectors.toMap(History::getOriginalFile, h -> h));

        List<UploadInitResponse.Item> items = IntStream.range(0, histories.size())
                .mapToObj(i -> {
                    String key = s3Keys.get(i);
                    History h = byKey.get(key);
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

        // 1) uuid 검증 + S3 존재 확인 + Magic Bytes 검증
        for (History h : histories) {
            if (!Objects.equals(h.getUuid(), req.uuid())) {
                throw new UnauthorizedAccessException("uuid가 일치하지 않는 history가 포함되어 있습니다. historyId=" + h.getHistoryId());
            }
            if (!s3StorageService.exists(h.getOriginalFile())) {
                throw new FileUploadNotConfirmedException("S3 업로드가 확인되지 않았습니다. key=" + h.getOriginalFile());
            }
            validateMagicBytes(h.getOriginalFile(), h.getOriginalFile());
        }

        // 2) DB 상태 변경 + outbox 생성은 HistoryService에게 위임
        historyService.markUploadedAndCreateConvertOutbox(histories);
        return historyService.getByIds(req.historyIds());
    }

    // 파일 앞 바이트로 실제 파일 형식 검증 (확장자 위장 방지)
    private void validateMagicBytes(String s3Key, String fileName) {
        byte[] header = s3StorageService.readHeader(s3Key, 8);
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        boolean valid = switch (ext) {
            case "png"        -> header[0] == (byte) 0x89 && header[1] == 0x50;
            case "jpg", "jpeg"-> header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
            case "gif"        -> header[0] == 0x47 && header[1] == 0x49; // GI
            case "bmp"        -> header[0] == 0x42 && header[1] == 0x4D; // BM
            case "pdf"        -> header[0] == 0x25 && header[1] == 0x50; // %P
            default           -> false;
        };

        if (!valid) {
            s3StorageService.delete(s3Key);
            throw new InvalidFileException("허용되지 않는 파일 형식입니다. key=" + s3Key);
        }
    }

    private String buildS3Key(String uuid, String s3FileName) {
        return prefix + "/" + uuid + "/" + s3FileName;
    }

}
