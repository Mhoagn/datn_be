package com.example.demo.service.impl;

import com.example.demo.dto.NotificationDTO.NotificationResponse;
import com.example.demo.dto.NotificationDTO.UnreadNotificationResponse;
import com.example.demo.dto.NotificationDTO.UpdateNotificationResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.entity.UserNotification;
import com.example.demo.exception.NotificationIsNotOfUser;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.repository.UserNotificationRepository;
import com.example.demo.service.interf.NotificationInterface;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationInterface {

    private final UserNotificationRepository userNotificationRepository;
    private final SecurityUtil securityUtil;
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketService notificationWebSocketService;

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<NotificationResponse> getNotifications(int page, int size) {
        Long currentUserId = securityUtil.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);

        Slice<UserNotification> slice = userNotificationRepository
                .findAllByUserIdWithDetails(currentUserId, pageable);

        List<NotificationResponse> content = slice.getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return SliceResponse.<NotificationResponse>builder()
                .content(content)
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationResponse getUnreadNotifications(int page, int size) {
        Long currentUserId = securityUtil.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);

        Slice<UserNotification> slice = userNotificationRepository
                .findUnreadByUserIdWithDetails(currentUserId, pageable);

        List<NotificationResponse> notifications = slice.getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        int unreadCount = userNotificationRepository.countUnreadByUserId(currentUserId);

        return UnreadNotificationResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public UpdateNotificationResponse updateStatusNotification(Long notificationId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        UserNotification userNotification = userNotificationRepository
                .findByUserIdAndNotificationId(currentUserId, notificationId)
                .orElseThrow(() -> new NotificationIsNotOfUser("Không tìm thấy thông báo cho người dùng"));

        // Nếu đã đọc rồi thì không cần update
        if (Boolean.TRUE.equals(userNotification.getIsRead())) {
            return UpdateNotificationResponse.builder()
                    .notificationId(notificationId)
                    .isRead(userNotification.getIsRead())
                    .readAt(userNotification.getReadAt())
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        userNotification.setIsRead(true);
        userNotification.setReadAt(now);
        userNotificationRepository.save(userNotification);

        // Broadcast unread count update qua WebSocket
        try {
            int newUnreadCount = userNotificationRepository.countUnreadByUserId(currentUserId);
            notificationWebSocketService.sendUnreadCountUpdate(currentUserId, newUnreadCount);
        } catch (Exception e) {
            // Log nhưng không throw để không ảnh hưởng response
        }

        return UpdateNotificationResponse.builder()
                .notificationId(notificationId)
                .isRead(true)
                .readAt(now)
                .build();
    }
}
