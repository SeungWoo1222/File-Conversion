package com.toyboyz.fileconversion.api.stats;

import com.toyboyz.fileconversion.api.stats.dto.StatsSummaryResponse;
import com.toyboyz.fileconversion.api.stats.service.StatsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsQueryService statsSummaryService;

    @GetMapping("/summary")
    public StatsSummaryResponse getSummary() {
        return statsSummaryService.getSummary();
    }
}