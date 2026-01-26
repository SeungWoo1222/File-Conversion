package com.toyboyz.fileconversion.api.repository;

import com.toyboyz.fileconversion.api.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History,Long> {
}
