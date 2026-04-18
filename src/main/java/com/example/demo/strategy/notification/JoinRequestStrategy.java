package com.example.demo.strategy.notification;

import com.example.demo.entity.GroupMember;
import com.example.demo.entity.Notification;
import com.example.demo.event.NotificationEvent;
import com.example.demo.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JoinRequestStrategy implements NotificationStrategy {

    private final GroupMemberRepository groupMemberRepository;

    @Override
    public Notification.Type getSupportedType() {
        return Notification.Type.JOIN_REQUEST;
    }

    @Override
    public List<Long> resolveRecipients(NotificationEvent event) {
        return groupMemberRepository
                .findUserIdsByGroupIdAndRole(event.getGroupId(), GroupMember.Role.OWNER);
    }
}
