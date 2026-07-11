package com.example.demo.strategy.notification;

import com.example.demo.entity.Notification;
import com.example.demo.event.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiSummaryReadyStrategy implements NotificationStrategy {

    @Override
    public Notification.Type getSupportedType() {
        return Notification.Type.AI_SUMMARY_READY;
    }

    @Override
    public List<Long> resolveRecipients(NotificationEvent event) {
        // Thông báo cho người đã yêu cầu tóm tắt AI
        return List.of(event.getActorId());
    }
}
