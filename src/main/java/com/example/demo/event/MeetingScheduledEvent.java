package com.example.demo.event;

public class MeetingScheduledEvent extends NotificationEvent {
    public MeetingScheduledEvent(Long actorId, Long groupId, Long meetingId) {
        super(actorId, groupId, meetingId, "Meetings");
    }
}
