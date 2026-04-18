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
public class NewMemberStrategy implements NotificationStrategy {

    private final GroupMemberRepository groupMemberRepository;

    @Override
    public Notification.Type getSupportedType() {
        return Notification.Type.NEW_MEMBER;
    }

    @Override
    public List<Long> resolveRecipients(NotificationEvent event) {
        return groupMemberRepository
                .findUserIdsByGroupIdAndRoleIn(
                        event.getGroupId(),
                        List.of(GroupMember.Role.OWNER, GroupMember.Role.MEMBER)
                );
    }
}
