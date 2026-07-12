package com.example.demo.event;

public class MeetingMemberReminderEvent extends NotificationEvent {
    public MeetingMemberReminderEvent(Long actorId, Long groupId, Long meetingId) {
        super(actorId, groupId, meetingId, "Meetings");
    }
}
