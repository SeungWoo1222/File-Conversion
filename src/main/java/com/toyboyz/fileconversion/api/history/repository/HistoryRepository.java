package com.toyboyz.fileconversion.api.history.repository;

import com.toyboyz.fileconversion.api.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History,Long> {

    List<History> findAllByUuidAndStatus(String uuid,String status);
}
