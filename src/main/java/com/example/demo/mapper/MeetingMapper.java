package com.example.demo.mapper;

import com.example.demo.dto.MeetingDTO.MeetingEndResponse;
import com.example.demo.dto.MeetingDTO.MeetingResponse;
import com.example.demo.entity.Meeting;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {

    public MeetingResponse toResponse(Meeting meeting) {
        return MeetingResponse.builder()
                .id(meeting.getId())
                .groupId(meeting.getGroup().getId())
                .createdBy(meeting.getCreator().getId())
                .startedAt(meeting.getStartedAt())
                .status(meeting.getStatus().name())
                .liveKitRoomName(meeting.getLiveKitRoomName())
                .build();
    }

    public MeetingEndResponse toEndResponse(Meeting meeting) {
        return MeetingEndResponse.builder()
                .id(meeting.getId())
                .groupId(meeting.getGroup().getId())
                .createdBy(meeting.getCreator().getId())
                .startedAt(meeting.getStartedAt())
                .status(meeting.getStatus().name())
                .endedAt(meeting.getEndedAt())
                .build();
    }
}
