package com.example.demo.dto.TranscriptDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptResponse {
    private Long id;
    private Long meetingRecordId;
    private List<TranscriptSegmentDTO> segments;
    private String fullText;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
