package com.toyboyz.fileconversion.worker.conversion.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class FilenameDTO {

    private String originFilename;
    private String convertedFilename;
    private String s3Key;

}
