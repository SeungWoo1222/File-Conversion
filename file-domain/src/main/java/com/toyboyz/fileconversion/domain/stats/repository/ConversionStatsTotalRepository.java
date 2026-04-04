package com.toyboyz.fileconversion.domain.stats.repository;

import com.toyboyz.fileconversion.domain.stats.entity.ConversionStatsTotal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionStatsTotalRepository extends JpaRepository<ConversionStatsTotal, String> {
}
