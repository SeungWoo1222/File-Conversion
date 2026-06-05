package com.toyboyz.fileconversion.api.global.handler;

import com.toyboyz.fileconversion.api.global.dto.ErrorResponse;
import com.toyboyz.fileconversion.api.global.exception.FileUploadNotConfirmedException;
import com.toyboyz.fileconversion.api.global.exception.InvalidFileException;
import com.toyboyz.fileconversion.api.global.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 허용되지 않는 파일 형식 (Magic Bytes 검증 실패)
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException e) {
        log.warn("[InvalidFileException] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, e.getMessage()));
    }

    // S3에 파일 없음 (업로드 없이 complete 호출)
    @ExceptionHandler(FileUploadNotConfirmedException.class)
    public ResponseEntity<ErrorResponse> handleUploadNotConfirmed(FileUploadNotConfirmedException e) {
        log.warn("[FileUploadNotConfirmedException] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, e.getMessage()));
    }

    // uuid 불일치 (남의 파일 접근 시도)
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException e) {
        log.warn("[UnauthorizedAccessException] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, e.getMessage()));
    }

    // 예상 못한 서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnexpectedException] {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "서버 내부 오류가 발생했습니다."));
    }
}
