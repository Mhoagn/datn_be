package com.example.demo.service.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class GroupWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyUserGroupJoined(Long userId, Long groupId, String groupName) {
        GroupJoinNotification notification = GroupJoinNotification.builder()
                .type("GROUP_JOIN_APPROVED")
                .groupId(groupId)
                .groupName(groupName)
                .timestamp(LocalDateTime.now())
                .message("Bạn đã được chấp nhận vào nhóm " + groupName)
                .build();

        String destination = "/queue/notifications";
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, notification);

        log.info("Notified user {} about joining group {}", userId, groupId);
    }
    
    // Broadcast meeting mới đến tất cả thành viên đang xem group
    public void broadcastNewMeeting(Long groupId, Object meetingData) {
        MeetingBroadcast broadcast = MeetingBroadcast.builder()
                .type("NEW_MEETING")
                .meeting(meetingData)
                .timestamp(LocalDateTime.now())
                .build();
        
        String destination = "/topic/group." + groupId;
        messagingTemplate.convertAndSend(destination, broadcast);
        
        log.info("Broadcasted new meeting to group {}", groupId);
    }
    
    // Broadcast meeting status update (end, etc.) đến tất cả thành viên đang xem group
    public void broadcastMeetingUpdate(Long groupId, Object meetingData) {
        MeetingBroadcast broadcast = MeetingBroadcast.builder()
                .type("MEETING_UPDATED")
                .meeting(meetingData)
                .timestamp(LocalDateTime.now())
                .build();
        
        String destination = "/topic/group." + groupId;
        messagingTemplate.convertAndSend(destination, broadcast);
        
        log.info("Broadcasted meeting update to group {}", groupId);
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingBroadcast {
        private String type;
        private Object meeting;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupJoinNotification {
        private String type;
        private Long groupId;
        private String groupName;
        private String message;
        private LocalDateTime timestamp;
    }
}
