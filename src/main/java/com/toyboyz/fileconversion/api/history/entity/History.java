package com.toyboyz.fileconversion.api.history.entity;

import com.toyboyz.fileconversion.config.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity(name = "history")
public class History extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    private String uuid;

    // 원본 파일명
    @Column(name = "file_name")
    private String fileName;

    // s3에 저장된 파일명
    @Column(name = "s3_file_name")
    private String s3FileName;

    //사용자가 요청한 포맷
    @Column(name = "request_format", length = 10)
    private String requestFormat;

    //원본 파일 경로
    @Column(name = "original_file",length = 1024)
    private String originalFile;

    //원본 파일 포맷
    @Column(name = "original_format",length = 10)
    private String originalFormat;

    //원본 파일 사이즈
    @Column(name = "original_file_size_bytes", nullable = false)
    @Builder.Default
    private Long originalFileSizeBytes = 0L;

    //변환 파일 경로
    @Column(name = "converted_file",length = 1024)
    private String convertedFile;
    //변환 후 포먓
    @Column(name = "converted_format",length = 10)
    private String convertedFormat;


    //파일 변환 상태
    @Column(length = 1)
    private String status;



    //레디스에서 넘어온 데이터를 통해 history 를 갱신
    public void updateHistory(String status,String convertedFile) {
        this.status=status;
        this.convertedFile=convertedFile;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
