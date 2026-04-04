package com.toyboyz.fileconversion.domain.history.repository;

import com.toyboyz.fileconversion.domain.history.entitiy.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
