package com.example.demo.service.interf;

import com.example.demo.dto.MeetingMessage.MeetingMessageResponse;
import com.example.demo.dto.MeetingMessage.SendMessageRequest;
import com.example.demo.dto.SliceResponse;

public interface MeetingMessageInterface {
    MeetingMessageResponse sendMessage(Long meetingId, SendMessageRequest request);
    SliceResponse<MeetingMessageResponse> getMessages(Long meetingId, int page, int size);
}
