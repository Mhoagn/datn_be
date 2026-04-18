package com.example.demo.event;

public class NewSummaryEvent extends NotificationEvent {
    public NewSummaryEvent(Long actorId, Long groupId, Long recordId) {
        super(actorId, groupId, recordId, "Meeting_Records");
    }
}
