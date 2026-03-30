package com.toyboyz.fileconversion.api.stats.repository;

import com.toyboyz.fileconversion.api.stats.entity.ConversionStatsTotal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionStatsTotalRepository extends JpaRepository<ConversionStatsTotal, String> {
}
