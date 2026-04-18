package com.example.demo.service.interf;

import com.example.demo.dto.MeetingDTO.*;
import com.example.demo.dto.MeetingRecordDTO.RecordListResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordStopResponse;
import com.example.demo.dto.SliceResponse;

public interface MeetingInterface {
    MeetingResponse createMeeting(CreateMeetingRequest createMeetingRequest);
    SliceResponse<MeetingResponse> getMeetings(Long groupId, int page, int size);
    MeetingJoinResponse joinMeeting(Long meetingId);
    MeetingLeaveResponse leaveMeeting(Long meetingId);
    MeetingEndResponse endMeeting(Long meetingId);
    RecordResponse startRecord(Long meetingId);
    RecordStopResponse stopRecord(Long meetingId);
    SliceResponse<RecordListResponse> getRecordsByGroupId(Long groupId, int page, int size);
}
