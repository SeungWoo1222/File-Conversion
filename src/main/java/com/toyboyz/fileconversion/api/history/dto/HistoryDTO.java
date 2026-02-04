package com.toyboyz.fileconversion.api.history.dto;

import jakarta.persistence.Column;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HistoryDTO {
    private Long historyId;
    private String uuid;
    private String convertedFormat;
    private String convertedFile;
    private String status;
}
