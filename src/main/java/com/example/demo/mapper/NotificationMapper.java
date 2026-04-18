package com.example.demo.mapper;

import com.example.demo.dto.NotificationDTO.NotificationResponse;
import com.example.demo.entity.UserNotification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(UserNotification un) {
        var n = un.getNotification();
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .actorId(n.getActorId())
                .actorName(n.getActor() != null ? n.getActor().getFullname() : null)
                .groupId(n.getGroupId())
                .groupName(n.getGroup() != null ? n.getGroup().getGroupName() : null)
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .isRead(un.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
