package com.example.demo.event;

public class AiSummaryReadyEvent extends NotificationEvent {
    public AiSummaryReadyEvent(Long actorId, Long groupId, Long recordId) {
        super(actorId, groupId, recordId, "Meeting_Records");
    }
}
