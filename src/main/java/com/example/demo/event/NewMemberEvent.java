package com.example.demo.event;

public class NewMemberEvent extends NotificationEvent {
    public NewMemberEvent(Long actorId, Long groupId, Long memberId) {
        super(actorId, groupId, memberId, "Group_Members");
    }
}
