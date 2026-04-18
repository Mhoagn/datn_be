package com.example.demo.dto.NotificationDTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadNotificationResponse {
    private List<NotificationResponse> notifications;
    private int unreadCount;
}
