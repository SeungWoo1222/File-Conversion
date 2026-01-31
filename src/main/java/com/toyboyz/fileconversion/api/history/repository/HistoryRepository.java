package com.toyboyz.fileconversion.api.history.repository;

import com.toyboyz.fileconversion.api.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History,Long> {
}
