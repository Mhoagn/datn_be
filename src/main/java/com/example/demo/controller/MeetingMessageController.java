package com.example.demo.controller;

import com.example.demo.dto.MeetingMessage.MeetingMessageResponse;
import com.example.demo.dto.MeetingMessage.SendMessageRequest;
import com.example.demo.dto.SliceResponse;
import com.example.demo.service.interf.MeetingMessageInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meeting_messages")
@RequiredArgsConstructor
public class MeetingMessageController {
    private final MeetingMessageInterface messageService;

    @PostMapping("/{meetingId}")
    public ResponseEntity<MeetingMessageResponse> sendMessage(
            @PathVariable Long meetingId,
            @RequestBody @Valid SendMessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(meetingId, request));
    }

    // Lấy lịch sử tin nhắn
    @GetMapping("/{meetingId}")
    public ResponseEntity<SliceResponse<MeetingMessageResponse>> getMessages(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(messageService.getMessages(meetingId, page, size));
    }
}
