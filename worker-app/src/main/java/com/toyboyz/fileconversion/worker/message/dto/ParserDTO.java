package com.toyboyz.fileconversion.worker.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) //자동으로 매핑할 때 매칭안되는 필드는 무시
public class ParserDTO {
    //msg 테이블에서 가져온 row를 파싱할 때 사용되는 DTO
    private String s3Key;
    private Long historyId;
    private String originalFileName;
    private String originalFormat;
    private String fileName;
    private String requestFormat;
    private String uuid;
}
