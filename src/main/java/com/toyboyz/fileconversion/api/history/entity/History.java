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

    //파일명
    @Column(name = "file_name")
    private String fileName;

    //사용자가 요청한 포맷
    @Column(name = "request_format", length = 10)
    private String requestFormat;

    //원본 파일 경로
    @Column(name = "original_file",length = 1024)
    private String originalFile;
    //원본 파일 포맷
    @Column(name = "original_format",length = 10)
    private String originalFormat;

    //변환 파일 경로
    @Column(name = "converted_file",length = 1024)
    private String convertedFile;
    //변환 후 포먓
    @Column(name = "converted_format",length = 10)
    private String convertedFormat;


    //파일 변환 상태
    @Column(length = 1)
    private String status;


    public void setStatus(String status) {
        this.status = status;
    }
}
