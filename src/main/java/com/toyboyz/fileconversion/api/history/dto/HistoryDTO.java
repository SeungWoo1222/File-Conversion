package com.toyboyz.fileconversion.api.history.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class HistoryDTO {
    private Long historyId;
    private String uuid;
    private String fileName;
    private String originalFile;
    private String convertedFile;
    private String status;
}
