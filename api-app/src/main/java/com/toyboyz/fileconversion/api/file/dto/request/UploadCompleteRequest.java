package com.toyboyz.fileconversion.api.file.dto.request;

import java.util.List;

public record UploadCompleteRequest(
        String uuid,
        List<Long> historyIds
) {}
