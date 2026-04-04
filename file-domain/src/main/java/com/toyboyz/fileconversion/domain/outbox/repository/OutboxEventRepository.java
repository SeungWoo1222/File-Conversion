package com.toyboyz.fileconversion.domain.outbox.repository;

import com.toyboyz.fileconversion.domain.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
}