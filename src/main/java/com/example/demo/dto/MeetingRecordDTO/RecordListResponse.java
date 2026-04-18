package com.example.demo.dto.MeetingRecordDTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordListResponse {
    private Long id;
    private Long meetingId;
    private String meetingTitle;
    private Long recordedBy;
    private String recordedByName;
    private String fileName;
    private String storageUrl;
    private Long fileSizeBytes;
    private Integer durationSeconds;
    private String status;
    private Boolean hasFinalSummary;  // Có bản tóm tắt cuối cùng chưa
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
