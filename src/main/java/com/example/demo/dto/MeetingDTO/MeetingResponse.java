package com.example.demo.dto.MeetingDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MeetingResponse {
    private Long id;
    private Long groupId;
    private Long createdBy;
    private String meetingTitle;
    private LocalDateTime startedAt;
    private LocalDateTime scheduledStartAt;
    private String status;
    private String liveKitRoomName;
}