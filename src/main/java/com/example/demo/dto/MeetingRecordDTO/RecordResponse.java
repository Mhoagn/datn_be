package com.example.demo.dto.MeetingRecordDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RecordResponse {
    private Long id;
    private Long meetingId;
    private Long recordedBy;
    private String egressId;
    private String status;
    private LocalDateTime createdAt;
}
