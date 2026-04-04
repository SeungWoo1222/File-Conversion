package com.toyboyz.fileconversion.api.history.repository;

public interface HistoryStatsProjection {
    long getCompletedCount();
    long getCompletedBytes();
}
