package com.toyboyz.fileconversion.infra.s3.service;

import com.toyboyz.fileconversion.common.slack.SlackNotifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final SlackNotifier slackNotifier;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final int day = 1;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${s3.presign-ttl-seconds:900}")
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



    //서버에서 변환 처리를 위해 내려받는 메서드 (서버 전용)
    public byte[] downloadFile(String key) {
        //Get 메서드 활성화 bucket 을 대상으로
        //bucket name + key 로 요청 생성
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(get)) {
            return inputStream.readAllBytes();
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("S3에 해당 파일이 없습니다. key: " + key);
        } catch (IOException e) {
            throw new RuntimeException("파일을 읽는 중 네트워크/IO 에러가 발생했습니다.", e);
        } catch (S3Exception e) {
            throw new RuntimeException("AWS S3 서비스 에러: " + e.awsErrorDetails().errorMessage());
        }
    }


    //서버가 파일을 변환 후 s3 의 uuid 디렉토리에 넣어줄 때 사용하는 메서드
    public void uploadFile(String key, byte[] file, String contentType) {
        try {
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(put, RequestBody.fromBytes(file));
        } catch (S3Exception e) {
            throw new RuntimeException("AWS 업로드 중 에러가 발생: " + e.getMessage());
        }
    }


    //s3 생명 주기 설정
    @PostConstruct
    public void initS3Lifecycle() {
        try {
            //규칙 생성, 1일 뒤 삭제
            LifecycleRule rule = LifecycleRule.builder()
                    .id("delete-old-converted-file-rule")
                    .filter(LifecycleRuleFilter.builder().prefix("").build())
                    .expiration(LifecycleExpiration.builder().days(day).build())
                    .status(ExpirationStatus.ENABLED)
                    .build();

            //설정 구성
            BucketLifecycleConfiguration lifecycleConfiguration = BucketLifecycleConfiguration.builder()
                    .rules(Collections.singletonList(rule))
                    .build();

            PutBucketLifecycleConfigurationRequest put = PutBucketLifecycleConfigurationRequest.builder()
                    .bucket(bucket)
                    .lifecycleConfiguration(lifecycleConfiguration)
                    .build();

            s3Client.putBucketLifecycleConfiguration(put);

            log.info(bucket + " 의 s3의 파일은 " + day + "일 후 자동 삭제됩니다.");
        } catch (S3Exception e) {
            slackNotifier.sendSlackNotification("s3 생명 주기 설정 오류 발생");
            log.info("s3 정책 설정 오류 발생 : {}",e.awsErrorDetails().errorMessage());
        }
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


