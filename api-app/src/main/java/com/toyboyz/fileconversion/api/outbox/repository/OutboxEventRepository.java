package com.toyboyz.fileconversion.api.outbox.repository;


import com.toyboyz.fileconversion.api.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // 오래된 이벤트 청크 단위로 조회 (대량 삭제 시 DB 부하 분산용)
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE created_at < :before
            LIMIT :limit
            """, nativeQuery = true)
    List<OutboxEvent> findOldEvents(
            @Param("before") LocalDateTime before,
            @Param("limit") int limit
    );
}
