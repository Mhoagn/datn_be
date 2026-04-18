package com.example.demo.strategy.notification;

import com.example.demo.entity.Notification;
import com.example.demo.event.NotificationEvent;

import java.util.List;

public interface NotificationStrategy {
    // Type mà strategy này xử lý
    Notification.Type getSupportedType();

    // Xác định danh sách userId sẽ nhận thông báo
    List<Long> resolveRecipients(NotificationEvent event);
}
