package com.toyboyz.fileconversion.domain.stats.entity;

import com.toyboyz.fileconversion.domain.common.BaseTime;
import jakarta.persistence.*;
import lombok.*;

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

    public ConversionStatsTotal(String statsKey, Long completedCount, Long completedBytes) {
        this.statsKey = statsKey;
        this.completedCount = completedCount;
        this.completedBytes = completedBytes;
    }

    public void overwrite(long completedCount, long completedBytes) {
        this.completedCount = completedCount;
        this.completedBytes = completedBytes;
    }
}

