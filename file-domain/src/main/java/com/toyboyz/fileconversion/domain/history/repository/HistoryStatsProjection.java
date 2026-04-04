package com.toyboyz.fileconversion.domain.history.repository;

public interface HistoryStatsProjection {
    long getCompletedCount();
    long getCompletedBytes();
}
