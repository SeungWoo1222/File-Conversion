package com.toyboyz.fileconversion.debezium.repository;

import com.toyboyz.fileconversion.debezium.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
}