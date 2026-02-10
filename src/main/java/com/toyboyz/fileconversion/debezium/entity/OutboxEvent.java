package com.toyboyz.fileconversion.debezium.entity;

import com.toyboyz.fileconversion.config.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity(name="outbox_event")
public class OutboxEvent extends BaseTime {

    @Id
    @Column(length = 36)
    private String id;  // UUID 문자열

    // 도메인 분류(차후 확장 가능성)
    // ex) file, thumbnail, metadata
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId; // 예: historyId or uuid

    // 발생한 사건 (차후 확장 가능성)
    // ex) FileConvertRequested, AlertRequired 등등
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON 문자열

    // 기본 값
    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }
}
