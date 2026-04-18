package com.example.demo.controller;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.conversationDTO.ConversationResponse;
import com.example.demo.dto.conversationDTO.CreateConversationRequest;
import com.example.demo.dto.conversationDTO.CreateOrGetConversationResult;
import com.example.demo.dto.messageDTO.MarkAsReadResponse;
import com.example.demo.service.interf.ConversationInterface;
import com.example.demo.service.interf.MessageInterface;
import com.example.demo.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationInterface conversationService;
    private final SecurityUtil securityUtil;
    private final MessageInterface messageService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrGetConversation(
            @RequestBody @Valid CreateConversationRequest request) {
        CreateOrGetConversationResult result =
                conversationService.createOrGetConversation(request.getUserEmail());

        return ResponseEntity
                .status(result.isNew() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(Map.of("conversation", result.getConversationResponse()));
    }

    @GetMapping("/my-conversations")
    public ResponseEntity<SliceResponse<ConversationResponse>> getConversations(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        SliceResponse<ConversationResponse> result =
                conversationService.getConversations(page, size);

        return ResponseEntity.ok(result);
    }

    // ConversationController.java
    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<MarkAsReadResponse> markAsRead(@PathVariable Long conversationId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        MarkAsReadResponse response = messageService.markAsRead(currentUserId, conversationId);
        return ResponseEntity.ok(response);
    }
}
