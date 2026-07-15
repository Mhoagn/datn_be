package com.example.demo.dto.MeetingRecordDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPlayUrlResponse {
    private Long recordId;
    private String playUrl;
    private long expiresInMinutes;
}
