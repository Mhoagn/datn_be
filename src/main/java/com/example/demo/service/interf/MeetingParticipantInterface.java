package com.example.demo.service.interf;

import com.example.demo.dto.MeetingParticipantDTO.MeetingParticipantResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.entity.MeetingParticipant;

public interface MeetingParticipantInterface {
    SliceResponse<MeetingParticipantResponse> getParticipants(Long meetingId, int page, int size);
}
