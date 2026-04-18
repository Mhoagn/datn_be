package com.example.demo.service.impl;

import com.example.demo.dto.NotificationDTO.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast notification đến 1 user cụ thể
     * Topic: /user/{userId}/notifications
     */
    public void sendNotificationToUser(Long userId, NotificationResponse notification) {
        try {
            String destination = "/queue/notifications";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "NEW_NOTIFICATION");
            payload.put("data", notification);
            payload.put("timestamp", LocalDateTime.now());
            
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, (Object) payload);
            
            log.info("📨 Sent notification to user {}: type={}, notificationId={}", 
                userId, notification.getType(), notification.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcast notification đến nhiều users
     */
    public void sendNotificationToUsers(List<Long> userIds, NotificationResponse notification) {
        userIds.forEach(userId -> sendNotificationToUser(userId, notification));
    }

    /**
     * Broadcast unread count update đến user
     * Topic: /user/{userId}/notifications/count
     */
    public void sendUnreadCountUpdate(Long userId, int unreadCount) {
        try {
            String destination = "/queue/notifications";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "UNREAD_COUNT_UPDATE");
            payload.put("unreadCount", unreadCount);
            payload.put("timestamp", LocalDateTime.now());
            
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, (Object) payload);
            
            log.debug("📊 Sent unread count to user {}: count={}", userId, unreadCount);
        } catch (Exception e) {
            log.error("❌ Failed to send unread count to user {}: {}", userId, e.getMessage());
        }
    }
}
