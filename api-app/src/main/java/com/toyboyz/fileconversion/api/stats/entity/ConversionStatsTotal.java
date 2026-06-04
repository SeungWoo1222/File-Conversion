package com.toyboyz.fileconversion.api.stats.entity;

import com.toyboyz.fileconversion.api.global.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)@Getter
@Entity
@Table(name = "conversion_stats_total")
public class ConversionStatsTotal extends BaseTime {
    @Id
    @Column(name = "stats_key", nullable = false, length = 20)
    private String statsKey;

    @Column(name = "completed_count", nullable = false)
    private Long completedCount;

    @Column(name = "completed_bytes", nullable = false)
    private Long completedBytes;

    // 마지막 정합성 보정 기준 시각 (델타 집계 시작점)
    @Column(name = "last_reconciled_at")
    private LocalDateTime lastReconciledAt;

    public ConversionStatsTotal(String statsKey, Long completedCount, Long completedBytes) {
        this.statsKey = statsKey;
        this.completedCount = completedCount;
        this.completedBytes = completedBytes;
    }

    public void overwrite(long completedCount, long completedBytes) {
        this.completedCount = completedCount;
        this.completedBytes = completedBytes;
    }

    public void overwriteWithTime(long completedCount, long completedBytes, LocalDateTime reconciledAt) {
        this.completedCount = completedCount;
        this.completedBytes = completedBytes;
        this.lastReconciledAt = reconciledAt;
    }
}

