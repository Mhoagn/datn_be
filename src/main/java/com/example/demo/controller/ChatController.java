package com.example.demo.controller;

import com.example.demo.dto.messageDTO.*;
import com.example.demo.service.impl.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send/{conversationId}")  // client gửi đến /app/chat.send/{id}
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Payload MessageRequest request,
            Principal principal) {                  // principal = userId từ interceptor

        System.out.println("[STOMP] Received /chat.send for conversation: " + conversationId);
        Long senderId = Long.parseLong(principal.getName());

        MessageResponse response = messageService.sendMessage(senderId, conversationId, request);
        System.out.println("[STOMP] Message saved with ID: " + response.getId());

        // Push tin nhắn đến tất cả người trong conversation
        System.out.println("[STOMP] Broadcasting to /topic/conversation." + conversationId);
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                response
        );
        
        // Push thông báo đến user cá nhân (người nhận) để cập nhật danh sách conversation
        messageService.notifyNewMessage(response);
    }

    @MessageMapping("/chat.delete")
    public void deleteMessage(
            @Payload DeleteMessageRequest request,
            Principal principal) {

        System.out.println("[STOMP] Received /chat.delete with messageId: " + request.getMessageId());

        try {
            Long currentUserId = Long.parseLong(principal.getName());
            MessageResponse response = messageService.deleteMessage(currentUserId, request.getMessageId());

            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("type", "MESSAGE_DELETED");
            responsePayload.put("message", response);

            System.out.println("[STOMP] Broadcasting MESSAGE_DELETED to topic /topic/conversation." + response.getConversationId());

            messagingTemplate.convertAndSend(
                    "/topic/conversation." + response.getConversationId(),
                    (Object) responsePayload
            );
        } catch (Exception e) {
            System.err.println("[STOMP] Error processing /chat.delete: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.read")
    public void readMessage(
            @Payload ReadMessageRequest request,
            Principal principal) {

        Long currentUserId = Long.parseLong(principal.getName());

        MarkAsReadResponse response = messageService.markAsRead(
                currentUserId, request.getConversationId());

        // Push thông báo đã đọc đến người gửi
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MESSAGES_READ");
        payload.put("data", response);

        messagingTemplate.convertAndSend(
                "/topic/conversation." + request.getConversationId(),
                (Object) payload
        );
    }
}
