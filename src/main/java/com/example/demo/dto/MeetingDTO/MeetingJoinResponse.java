package com.example.demo.dto.MeetingDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MeetingJoinResponse {
    private Long id;
    private Long userId;
    private Integer sessionIndex;
    private LocalDateTime joinedAt;
    private String serverUrl;         // LiveKit server URL
    private String participantToken;
}
