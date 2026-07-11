package com.example.demo.util;

import com.example.demo.entity.Notification;
import com.example.demo.event.*;
import com.example.demo.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationDispatcher notificationDispatcher;

    @Async // không block luồng chính
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewPost(NewPostEvent event) {
        notificationDispatcher.dispatch(Notification.Type.NEW_POST, event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewMember(NewMemberEvent event) {
        notificationDispatcher.dispatch(Notification.Type.NEW_MEMBER, event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewMeeting(NewMeetingEvent event) {
        notificationDispatcher.dispatch(Notification.Type.NEW_MEETING, event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewSummary(NewSummaryEvent event) {
        notificationDispatcher.dispatch(Notification.Type.NEW_SUMMARY, event);
    }

    @Async
    @EventListener
    public void handleAiSummaryReady(AiSummaryReadyEvent event) {
        try {
            notificationDispatcher.dispatch(Notification.Type.AI_SUMMARY_READY, event);
        } catch (Exception e) {
            log.error("Failed to dispatch AI_SUMMARY_READY notification: {}", e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJoinRequest(JoinRequestEvent event) {
        notificationDispatcher.dispatch(Notification.Type.JOIN_REQUEST, event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingScheduled(MeetingScheduledEvent event) {
        try {
            notificationDispatcher.dispatch(Notification.Type.MEETING_SCHEDULED, event);
        } catch (Exception e) {
            log.error("Failed to dispatch MEETING_SCHEDULED notification: {}", e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingCreatorReminder(MeetingCreatorReminderEvent event) {
        try {
            notificationDispatcher.dispatch(Notification.Type.MEETING_REMINDER_CREATOR, event);
        } catch (Exception e) {
            log.error("Failed to dispatch MEETING_REMINDER_CREATOR notification: {}", e.getMessage(), e);
        }
    }
}
