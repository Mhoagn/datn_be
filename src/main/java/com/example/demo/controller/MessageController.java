package com.example.demo.controller;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.messageDTO.DeleteMessageRequest;
import com.example.demo.dto.messageDTO.MessageResponse;
import com.example.demo.service.interf.MessageInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageInterface messageService;

    @GetMapping("/{conversationId}")
    public ResponseEntity<SliceResponse<MessageResponse>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        SliceResponse<MessageResponse> result =
                messageService.getConversationMessages(conversationId, page, size);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @RequestBody DeleteMessageRequest deleteMessageRequest) {

        MessageResponse response = messageService.deleteMessage(deleteMessageRequest);

        return ResponseEntity.ok(Map.of("message", response));
    }

    @PostMapping("/conversations/{conversationId}/attachments")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @PathVariable Long conversationId,
            @RequestParam("file") MultipartFile file) {

        MessageResponse response = messageService.uploadAttachment(conversationId, file);

        return ResponseEntity.ok(Map.of("message", response));
    }
}
