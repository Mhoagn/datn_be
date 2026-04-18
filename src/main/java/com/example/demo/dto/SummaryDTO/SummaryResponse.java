package com.example.demo.dto.SummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private Long id;
    private Long meetingRecordId;
    private String aiModel;
    private String rawSummary;
    private List<SummaryPointDTO> summaryPoints;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
