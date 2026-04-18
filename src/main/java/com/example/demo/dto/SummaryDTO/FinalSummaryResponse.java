package com.example.demo.dto.SummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalSummaryResponse {
    private Long id;
    private Long meetingRecordId;
    private Long createdBy;
    private String createdByName;
    private String finalContent;
    private List<Long> selectedPointIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
