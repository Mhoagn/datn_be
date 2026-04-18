package com.example.demo.mapper;

import com.example.demo.dto.MeetingMessage.MeetingMessageResponse;
import com.example.demo.entity.MeetingMessage;
import org.springframework.stereotype.Component;

@Component
public class MeetingMessageMapper {
    public MeetingMessageResponse toResponse(MeetingMessage message) {
        return MeetingMessageResponse.builder()
                .id(message.getId())
                .meetingId(message.getMeeting().getId())
                .authorId(message.getAuthor().getId())
                .authorName(message.getAuthor().getFullname())
                .authorAvatar(message.getAuthor().getAvatarUrl())
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .sentAt(message.getSentAt())
                .build();
    }
}
