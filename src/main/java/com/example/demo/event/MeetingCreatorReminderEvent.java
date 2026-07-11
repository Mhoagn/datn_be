package com.example.demo.event;

public class MeetingCreatorReminderEvent extends NotificationEvent {
    public MeetingCreatorReminderEvent(Long actorId, Long groupId, Long meetingId) {
        super(actorId, groupId, meetingId, "Meetings");
    }
}
