package com.example.demo.util;

import com.example.demo.entity.Conversation;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.service.impl.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final UserStatusService userStatusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    // Khi user connect WebSocket
    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal == null) return;

        Long userId = Long.parseLong(principal.getName());
        log.info("User {} connected", userId);

        // 1. Cập nhật DB → ONLINE
        userStatusService.setOnline(userId);

        // 2. Push đến tất cả conversation của user này
        pushStatusToConversations(userId, "USER_ONLINE", null);
    }

    // Khi user disconnect WebSocket
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal == null) return;

        Long userId = Long.parseLong(principal.getName());
        log.info("User {} disconnected", userId);

        // 1. Cập nhật DB → OFFLINE
        userStatusService.setOffline(userId);

        // 2. Push đến tất cả conversation của user này
        pushStatusToConversations(userId, "USER_OFFLINE", LocalDateTime.now());
    }

    // ================================================================
    // Helper — push status đến tất cả conversation liên quan
    // ================================================================
    private void pushStatusToConversations(
            Long userId, String type, LocalDateTime lastActiveAt) {

        // Lấy tất cả conversation của user này
        List<Conversation> conversations =
                conversationRepository.findAllByUserIdNoPage(userId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("userId", userId);
        if (lastActiveAt != null) {
            payload.put("lastActiveAt", lastActiveAt);
        }

        // Push vào từng conversation topic
        conversations.forEach(conversation ->
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + conversation.getId(),
                        (Object) payload
                )
        );
    }
}
