package com.toyboyz.fileconversion.api.file.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileConvertResponse {
    private Long historyId;
    private String uuid;
    private String status; // "P"
}