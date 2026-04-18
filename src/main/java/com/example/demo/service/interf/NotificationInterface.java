package com.example.demo.service.interf;

import com.example.demo.dto.NotificationDTO.NotificationResponse;
import com.example.demo.dto.NotificationDTO.UnreadNotificationResponse;
import com.example.demo.dto.NotificationDTO.UpdateNotificationResponse;
import com.example.demo.dto.SliceResponse;

public interface NotificationInterface {
    SliceResponse<NotificationResponse> getNotifications(int page, int size);
    UnreadNotificationResponse getUnreadNotifications(int page, int size);
    UpdateNotificationResponse updateStatusNotification(Long notificationId);
}
