package com.toyboyz.fileconversion.infra.redis.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
public class SubDTO {

    private Long historyId;
    private String uuid;
    private String fileName;
    private int percent;
    private String convertedFile; //완료된 파일명
    private String status;
    private int size;

}
