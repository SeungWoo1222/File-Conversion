package com.toyboyz.fileconversion.api.file.dto.request;

import java.util.List;

public record UploadInitRequest(
        String uuid,
        String targetFormat,
        List<FileMeta> files
) {
    public record FileMeta(
            String filename,
            String contentType,
            long size
    ) {}
}
