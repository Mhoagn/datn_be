package com.example.demo.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class NewPostEvent extends NotificationEvent {
    public NewPostEvent(Long actorId, Long groupId, Long postId) {
        super(actorId, groupId, postId, "Posts");
    }
}
