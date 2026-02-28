package com.toyboyz.fileconversion.s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.presign-ttl-seconds:900}")
    private long ttlSeconds;

    public String presignPutUrl(String key, String contentType) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(ttlSeconds))
                .putObjectRequest(put)
                .build();

        return s3Presigner.presignPutObject(presignReq).url().toString();
    }


    //클라이언트가 다운로드받을 때 사용하는 메서드(외부에서 s3 접근 시 사용)
    public String presignGetUrl(String key) {
        // 2. S3 객체 요청 생성 (강제 다운로드 속성 부여)
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(createHeader(key))
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(ttlSeconds))
                .getObjectRequest(get)
                .build();
        return s3Presigner.presignGetObject(presignReq).url().toString();
    }


    //다운로드시 새창으로 이동이 아닌 현재 화면에서 다운로드를 위한 명령어 생성 메서드
    //브라우저에게 보낼 헤더를 파일명과 함께 가공해서 리턴
    private String createHeader(String key) {
        String fileName = key.substring(key.lastIndexOf("/") + 1);
        return "attachment; filename=\"" + fileName + "\"";
    }


    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // 404 같은 경우도 여기로 올 수 있음
            return e.statusCode() != 404 ? true : false;
        }
    }
}


