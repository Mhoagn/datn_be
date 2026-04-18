package com.example.demo.service;

import com.example.demo.dto.NotificationDTO.NotificationResponse;
import com.example.demo.entity.Notification;
import com.example.demo.entity.UserNotification;
import com.example.demo.event.NotificationEvent;
import com.example.demo.factory.NotificationStrategyFactory;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserNotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.NotificationWebSocketService;
import com.example.demo.strategy.notification.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final NotificationStrategyFactory strategyFactory;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketService notificationWebSocketService;

    @Transactional
    public void dispatch(Notification.Type type, NotificationEvent event) {
        // 1. Lấy strategy phù hợp
        NotificationStrategy strategy = strategyFactory.getStrategy(type);

        // 2. Xác định recipients
        List<Long> recipientIds = strategy.resolveRecipients(event);
        if (recipientIds.isEmpty()) return;

        // 3. Tạo và lưu Notification
        Notification notification = new Notification();
        notification.setType(type);
        notification.setActor(userRepository.getReferenceById(event.getActorId()));
        notification.setGroup(groupRepository.getReferenceById(event.getGroupId()));
        notification.setReferenceId(event.getReferenceId());
        notification.setReferenceType(event.getReferenceType());
        notificationRepository.save(notification);

        // 4. Tạo UserNotification cho từng recipient (batch insert)
        List<UserNotification> userNotifications = recipientIds.stream()
                .map(uid -> {
                    UserNotification un = new UserNotification();
                    un.setUser(userRepository.getReferenceById(uid));
                    un.setNotification(notification);
                    un.setIsRead(false);
                    return un;
                })
                .collect(Collectors.toList());

        userNotificationRepository.saveAll(userNotifications);

        // 5. Broadcast notification qua WebSocket đến tất cả recipients
        try {
            // Map sang NotificationResponse
            NotificationResponse notificationResponse = notificationMapper.toResponse(
                userNotifications.get(0) // Dùng UserNotification đầu tiên để có đủ data
            );
            
            // Broadcast đến tất cả recipients
            notificationWebSocketService.sendNotificationToUsers(recipientIds, notificationResponse);
            
            log.info("✅ Broadcasted notification type={} to {} users", type, recipientIds.size());
        } catch (Exception e) {
            log.error("❌ Failed to broadcast notification via WebSocket: {}", e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến luồng chính
        }
    }
}
