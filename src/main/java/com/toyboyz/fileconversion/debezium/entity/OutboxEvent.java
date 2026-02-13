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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 36, nullable = false, updatable = false, unique = true)
    private String uuid; // 이벤트 식별용 uuid

    // 도메인 분류
    // ex) file, alert
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private Long aggregateId; // ex) historyId, alertId

    // 발생한 사건
    // ex) FileConvertRequested, AlertRequired 등등
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    // MQ에 제공할 History 메타 데이터
    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON 문자열

    // 기본 값
//    @PrePersist
//    void prePersist() {
//        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
//    }

    private OutboxEvent(String aggregateType,
                        Long aggregateId,
                        String eventType,
                        String payload) {

        this.uuid = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public static OutboxEvent of(String aggregateType,
                                 Long aggregateId,
                                 String eventType,
                                 String payload) {

        return new OutboxEvent(
                aggregateType,
                aggregateId,
                eventType,
                payload
        );
    }
}
