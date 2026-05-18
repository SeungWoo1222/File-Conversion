package com.toyboyz.fileconversion.api.stats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsSummaryResponse {
    private final long completedCount;
    private final long completedBytes;
    private final boolean realTime;
    private final String notice;
}
