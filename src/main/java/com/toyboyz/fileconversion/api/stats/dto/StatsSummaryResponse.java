package com.toyboyz.fileconversion.api.stats.dto;

import lombok.Builder;

@Builder
public record StatsSummaryResponse(
        long completedCount,
        long completedBytes
) {
}