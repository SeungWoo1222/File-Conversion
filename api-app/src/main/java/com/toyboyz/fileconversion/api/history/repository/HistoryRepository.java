package com.toyboyz.fileconversion.api.history.repository;


import com.toyboyz.fileconversion.api.history.entitiy.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History,Long> {

    List<History> findAllByUuidAndStatus(String uuid,String status);

    @Query(value = """
            SELECT COUNT(*) AS completedCount,
                   COALESCE(SUM(original_file_size_bytes), 0) AS completedBytes
            FROM history
            WHERE status = '3'
            """, nativeQuery = true)
    HistoryStatsProjection aggregateCompletedStats();

    // 특정 기간 내 완료된 항목만 집계 (델타 보정용)
    @Query(value = """
            SELECT COUNT(*) AS completedCount,
                   COALESCE(SUM(original_file_size_bytes), 0) AS completedBytes
            FROM history
            WHERE status = '3'
            AND updated_at BETWEEN :from AND :to
            """, nativeQuery = true)
    HistoryStatsProjection aggregateCompletedStatsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
