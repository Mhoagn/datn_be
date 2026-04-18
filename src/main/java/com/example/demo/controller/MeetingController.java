package com.example.demo.controller;

import com.example.demo.dto.MeetingDTO.*;
import com.example.demo.dto.MeetingRecordDTO.RecordListResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordStopResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.service.interf.MeetingInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingInterface meetingService;

    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @RequestBody @Valid CreateMeetingRequest request
    ) {
        MeetingResponse response = meetingService.createMeeting(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<SliceResponse<MeetingResponse>> getMeetings(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SliceResponse<MeetingResponse> response = meetingService.getMeetings(groupId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{meetingId}/join")
    public ResponseEntity<MeetingJoinResponse> joinMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.joinMeeting(meetingId));
    }

    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<MeetingLeaveResponse> leaveMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingService.leaveMeeting(meetingId));
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<MeetingEndResponse> endMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingService.endMeeting(meetingId));
    }

    @PostMapping("/{meetingId}/record/start")
    public ResponseEntity<RecordResponse> startRecord(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.startRecord(meetingId));
    }

    @PostMapping("/{meetingId}/record/stop")
    public ResponseEntity<RecordStopResponse> stopRecord(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.stopRecord(meetingId));
    }

    @GetMapping("/records")
    public ResponseEntity<SliceResponse<RecordListResponse>> getRecords(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SliceResponse<RecordListResponse> response = meetingService.getRecordsByGroupId(groupId, page, size);
        return ResponseEntity.ok(response);
    }
}
