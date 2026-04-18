package com.example.demo.dto.MeetingParticipantDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class MeetingParticipantResponse {
    private Long userId;
    private String userFullname;
    private String userEmail;
    private String avatarUrl;
}
