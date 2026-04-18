package com.example.demo.event;

public class NewMeetingEvent extends NotificationEvent {
    public NewMeetingEvent(Long actorId, Long groupId, Long meetingId) {
        super(actorId, groupId, meetingId, "Meetings");
    }
}
