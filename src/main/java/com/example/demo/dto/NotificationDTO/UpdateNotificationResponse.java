package com.example.demo.dto.NotificationDTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNotificationResponse {
    private Long notificationId;
    private Boolean isRead;
    private LocalDateTime readAt;
}