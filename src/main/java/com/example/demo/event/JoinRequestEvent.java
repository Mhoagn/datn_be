package com.example.demo.event;

public class JoinRequestEvent extends NotificationEvent {
    public JoinRequestEvent(Long actorId, Long groupId, Long requestId) {
        super(actorId, groupId, requestId, "Group_Join_Requests");
    }
}
