package com.example.demo.dto.NotificationDTO;

import com.example.demo.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Notification.Type type;
    private Long actorId;
    private String actorName;
    private Long groupId;
    private String groupName;
    private Long referenceId;
    private String referenceType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
