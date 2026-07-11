package com.example.demo.strategy.notification;

import com.example.demo.entity.Notification;
import com.example.demo.event.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingReminderCreatorStrategy implements NotificationStrategy {

    @Override
    public Notification.Type getSupportedType() {
        return Notification.Type.MEETING_REMINDER_CREATOR;
    }

    @Override
    public List<Long> resolveRecipients(NotificationEvent event) {
        return List.of(event.getActorId());
    }
}
