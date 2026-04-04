package com.toyboyz.fileconversion.api.outbox.repository;

import com.toyboyz.fileconversion.domain.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
}