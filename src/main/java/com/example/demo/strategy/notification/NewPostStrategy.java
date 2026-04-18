package com.example.demo.strategy.notification;

import com.example.demo.entity.Notification;
import com.example.demo.event.NotificationEvent;
import com.example.demo.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NewPostStrategy implements NotificationStrategy {

    private final GroupMemberRepository groupMemberRepository;

    @Override
    public Notification.Type getSupportedType() {
        return Notification.Type.NEW_POST;
    }

    @Override
    public List<Long> resolveRecipients(NotificationEvent event) {
        return groupMemberRepository
                .findUserIdsByGroupId(event.getGroupId())
                .stream()
                .filter(uid -> !uid.equals(event.getActorId())) // loại actor
                .collect(Collectors.toList());
    }
}
