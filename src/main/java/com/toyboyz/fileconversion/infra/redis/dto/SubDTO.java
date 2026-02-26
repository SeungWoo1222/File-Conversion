package com.toyboyz.fileconversion.infra.redis.dto;

import lombok.*;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class SubDTO {

    private String uuid;
    private String filename;
    private int percent;
    private String status;
//    private String convertedFile; 추후 완료된 파일 주소
}
