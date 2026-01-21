package com.toyboyz.fileconversion.api.entity;

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

    //원본 파일 경로
    @Column(name = "original_file",length = 512)
    private String originalFile;

    //변환 파일 경로
    @Column(name = "converted_file",length = 512)
    private String convertedFile;

    //파일 변환 상태
    @Column(length = 1)
    private String status;
}
