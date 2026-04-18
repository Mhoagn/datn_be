package com.example.demo.controller;

import com.example.demo.dto.MeetingParticipantDTO.MeetingParticipantResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.service.interf.MeetingParticipantInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingParticipantController {

    private final MeetingParticipantInterface meetingParticipantService;

    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<SliceResponse<MeetingParticipantResponse>> getParticipants(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                meetingParticipantService.getParticipants(meetingId, page, size)
        );
    }
}
