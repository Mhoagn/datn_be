package com.example.demo.util;

import com.example.demo.entity.Notification;
import com.example.demo.event.*;
import com.example.demo.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJoinRequest(JoinRequestEvent event) {
        notificationDispatcher.dispatch(Notification.Type.JOIN_REQUEST, event);
    }
}
