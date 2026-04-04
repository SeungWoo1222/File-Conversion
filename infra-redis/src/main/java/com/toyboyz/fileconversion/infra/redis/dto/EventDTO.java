package com.toyboyz.fileconversion.infra.redis.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class EventDTO {

    private Long historyId;
    private String uuid;
    private String fileName;
    private int percent;
    private String convertedFile;
    private String status;
    private int size;

}
