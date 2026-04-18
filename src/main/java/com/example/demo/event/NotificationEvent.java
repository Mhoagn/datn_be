package com.example.demo.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class NotificationEvent {
    private final Long actorId;
    private final Long groupId;
    private final Long referenceId;
    private final String referenceType;
}
