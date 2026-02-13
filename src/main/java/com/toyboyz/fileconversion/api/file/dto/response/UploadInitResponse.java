package com.toyboyz.fileconversion.api.file.dto.response;

import java.util.List;

public record UploadInitResponse(
        List<Item> items
) {
    public record Item(
            Long historyId,
            String s3Key,
            String uploadUrl
    ) {}
}
