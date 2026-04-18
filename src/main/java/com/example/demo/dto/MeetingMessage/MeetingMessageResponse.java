package com.example.demo.dto.MeetingMessage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MeetingMessageResponse {
    private Long id;
    private Long meetingId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private String messageType;
    private LocalDateTime sentAt;
}
