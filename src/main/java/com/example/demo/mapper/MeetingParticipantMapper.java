package com.example.demo.mapper;

import com.example.demo.dto.MeetingParticipantDTO.MeetingParticipantResponse;
import com.example.demo.entity.MeetingParticipant;
import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MeetingParticipantMapper {
    public MeetingParticipantResponse toResponse(MeetingParticipant participant) {
        User user = participant.getUser();
        return MeetingParticipantResponse.builder()
                .userId(user.getId())
                .userFullname(user.getFullname())   // đổi tên method cho khớp với entity User của bạn
                .userEmail(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
